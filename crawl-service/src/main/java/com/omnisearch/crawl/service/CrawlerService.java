package com.omnisearch.crawl.service;

import com.omnisearch.common.PageCrawledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CrawlerService {

    // Manual SLF4J Logger (bypasses Lombok)
    private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    // Manual Constructor for Spring Dependency Injection (bypasses Lombok)
    public CrawlerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String TOPIC = "web-content-topic";

    // Java 21 Magic: Thousands of Virtual Threads natively!
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public void startCrawling(String seedUrl, int maxDepth) {
        log.info("Starting crawler for {} up to depth {}", seedUrl, maxDepth);

        // Thread-safe Set to prevent infinite loops and duplicate crawls
        Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
        BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();

        queue.add(new CrawlTask(seedUrl, 0));
        visitedUrls.add(seedUrl);

        // Track how many tasks are running so we know when to shut down
        AtomicInteger activeTasks = new AtomicInteger(1);

        // Run the orchestrator asynchronously
        CompletableFuture.runAsync(() -> {
            while (activeTasks.get() > 0 || !queue.isEmpty()) {
                CrawlTask task = queue.poll();
                if (task != null) {
                    // Fire-and-forget virtual thread
                    CompletableFuture.runAsync(() -> processTask(task, maxDepth, visitedUrls, queue, activeTasks), executor);
                } else {
                    try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            }
            log.info("Crawling completely finished for seed: {}", seedUrl);
        }, executor);
    }

    private void processTask(CrawlTask task, int maxDepth, Set<String> visitedUrls, BlockingQueue<CrawlTask> queue, AtomicInteger activeTasks) {
        try {
            if (task.depth() > maxDepth) return;

            log.info("Crawling: {} (Depth: {})", task.url(), task.depth());

            // Jsoup downloads the HTML
            Document doc = Jsoup.connect(task.url()).timeout(5000).get();
            String title = doc.title();
            String textContent = doc.body().text(); // Magically strips HTML tags!

            // 1. Send the data to Kafka! (This is our Producer)
            PageCrawledEvent event = new PageCrawledEvent(task.url(), title, textContent);
            kafkaTemplate.send(TOPIC, task.url(), event);

            // 2. Find new links to crawl (Breadth-First Search)
            if (task.depth() < maxDepth) {
                doc.select("a[href]").forEach(link -> {
                    String nextUrl = link.absUrl("href");
                    // Only process HTTP links and ensure we haven't visited them yet
                    if (nextUrl.startsWith("http") && visitedUrls.add(nextUrl)) {
                        activeTasks.incrementAndGet(); // We found more work
                        queue.add(new CrawlTask(nextUrl, task.depth() + 1));
                    }
                });
            }
        } catch (Exception e) {
            log.warn("Failed to crawl: {} - {}", task.url(), e.getMessage());
        } finally {
            activeTasks.decrementAndGet(); // Mark task as finished
        }
    }

    // A lightweight Java 14+ Record to hold queue data
    record CrawlTask(String url, int depth) {}
}
