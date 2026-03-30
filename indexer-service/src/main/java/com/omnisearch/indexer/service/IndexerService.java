package com.omnisearch.indexer.service;
import com.omnisearch.common.PageCrawledEvent;
import com.omnisearch.indexer.entity.WebDocument;
import com.omnisearch.indexer.repository.WebDocumentRepository;
import org.springframework.kafka.annotation.*;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.slf4j.*;
@Service
public class IndexerService {
    private static final Logger log = LoggerFactory.getLogger(IndexerService.class);
    private final WebDocumentRepository repository;
    public IndexerService(WebDocumentRepository repository) { this.repository = repository; }
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2.0)
    )
    @KafkaListener(topics = "web-content-topic", groupId = "indexer-group")
    public void consume(PageCrawledEvent event) {
        log.info("Indexing URL: {}", event.getUrl());
        repository.save(new WebDocument(event.getUrl(), event.getTitle(), event.getContent()));
    }
    @DltHandler
    public void handleFailure(PageCrawledEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("PERSISTENT FAILURE: Dead Letter Queue (DLQ) Topic: {} | URL: {}", topic, event.getUrl());
    }
}