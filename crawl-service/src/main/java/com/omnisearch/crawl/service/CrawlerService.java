package com.omnisearch.crawl.service;

import com.omnisearch.common.PageCrawledEvent;
import com.omnisearch.crawl.util.CrawlUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CrawlerService {
    private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);

    private final KafkaTemplate<String, PageCrawledEvent> kafkaTemplate;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    //Thread-safe sets prevent infinite loops and duplicates
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final Set<String> seenContentHashes = ConcurrentHashMap.newKeySet(); //[phase 3]

    public CrawlerService(KafkaTemplate<String, PageCrawledEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void startCrawling(String seedUrl,int maxDepth){
        log.info("Initiating Breadth-First search at seed: {}",seedUrl);
        submitTask(seedUrl, 0, maxDepth);
    }

    private void submitTask(String url,int currentDepth,int maxDepth){
        if (currentDepth>maxDepth) return;

        // [Phase 3] URL Normalization (Prevents crawling http vs https versions of same site)
        String normalizedUrl = CrawlUtils.normalizeUrl(url);

        if (!visitedUrls.add(normalizedUrl)) return; // Abort if we've already been here

        //Spin up a ultra-lightweight Virtual Thread
        executorService.submit(()->processPage(normalizedUrl,currentDepth,maxDepth));
    }

    private void processPage(String url,int currentDepth,int maxDepth){
        try{
            log.info("Thread-{} is scraping: {}", Thread.currentThread().threadId(),url);

            Document doc = Jsoup.connect(url).timeout(5000).get();
            String cleanText = doc.body().text();

            //[phase 3] deduplication (Prevents indexing identical content from different URLs)
            String fingerprint = CrawlUtils.generateMd5Hash(cleanText);
            if (!seenContentHashes.add(fingerprint)){
                log.warn("Identical content already indexed. Skipping: {}",url);
                return;
            }

            //Ship the clean data to Kafka -> Indexer
            PageCrawledEvent event = new PageCrawledEvent(url,doc.title(),cleanText);
            kafkaTemplate.send("web-content-topic",event);

            //Extract links for the next level of BFS
            doc.select("a[href]").forEach(element->{
                String link = element.attr("abs:href");
                if (!link.isEmpty()){
                    submitTask(link,currentDepth+1,maxDepth);
                }
            });
        } catch(Exception e){
            log.error("Crawl error {}: {}",url,e.getMessage());
        }
    }
}