package com.omnisearch.crawl.service;
import com.omnisearch.common.PageCrawledEvent;
import com.omnisearch.crawl.util.CrawlUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
@Service
public class CrawlerService {
    private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);
    private final KafkaTemplate<String, PageCrawledEvent> kafkaTemplate;
    private final RobotsTxtService robotsService;
    private final ExecutorService threads = Executors.newVirtualThreadPerTaskExecutor();

    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Set<String> hashes = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> lastRequest = new ConcurrentHashMap<>();
    public CrawlerService(KafkaTemplate<String, PageCrawledEvent> kafkaTemplate, RobotsTxtService rs) {
        this.kafkaTemplate = kafkaTemplate;
        this.robotsService = rs;
    }
    public void startCrawling(String seedUrl, int maxDepth) {
        String allowedDomain = URI.create(seedUrl).getHost();
        submitTask(seedUrl, 0, maxDepth, allowedDomain);
    }
    private void submitTask(String url, int depth, int max, String domain) {
        if (depth > max) return;

        String cleanUrl = CrawlUtils.normalizeUrl(url);
        if (!visited.add(cleanUrl) || !robotsService.isAllowed(cleanUrl)) return;

        // Final Safety: Don't wander into other websites!
        if (!URI.create(cleanUrl).getHost().equals(domain)) return;
        threads.submit(() -> process(cleanUrl, depth, max, domain));
    }
    private void process(String url, int depth, int max, String domain) {
        try {
            // Rate Limiting: 1 second delay per domain
            long wait = 1000 - (System.currentTimeMillis() - lastRequest.getOrDefault(domain, 0L));
            if (wait > 0) Thread.sleep(wait);
            lastRequest.put(domain, System.currentTimeMillis());
            log.info("[LEVEL {}] Crawling: {}", depth, url);
            Document doc = Jsoup.connect(url).timeout(5000).get();
            String text = doc.body().text();
            if (hashes.add(CrawlUtils.generateMd5Hash(text))) {
                kafkaTemplate.send("web-content-topic", new PageCrawledEvent(url, doc.title(), text));
                doc.select("a[href]").forEach(el -> submitTask(el.attr("abs:href"), depth + 1, max, domain));
            } else {
                log.warn("Skipping Duplicate Content at: {}", url);
            }
        } catch (Exception e) { log.error("Failed: {} -> {}", url, e.getMessage()); }
    }
}