package com.omnisearch.indexer.service;

import com.omnisearch.common.PageCrawledEvent;
import com.omnisearch.indexer.entity.WebDocument;
import com.omnisearch.indexer.repository.WebDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class IndexerService {
    private static final Logger log = LoggerFactory.getLogger(IndexerService.class);
    private final WebDocumentRepository repository;

    public IndexerService(WebDocumentRepository repository){
        this.repository = repository;
    }

    //This annotation tells Spring: "Wait forever is the background. When a new message hits this topic, trigger this method immediately!"

    @KafkaListener(topics = "web-content-topic", groupId = "indexer-group")
    public void consumeCrawledPage(PageCrawledEvent event){
        log.info("Indexer magically received page from Kafka: {}", event.getUrl());

        try{
            //Convert our Event DTO into our Database Entity
            WebDocument doc = new WebDocument(event.getUrl(),event.getTitle(),event.getContent());

            //save it forever in Elasticsearch
            repository.save(doc);
            log.info("Successfully indexed document: {}", event.getUrl());
        }catch (Exception e){
            log.error("Failed to index page {}", event.getUrl(),e.getMessage());
        }
    }
}
