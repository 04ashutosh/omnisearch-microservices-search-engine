package com.omnisearch.crawl.controller;

import com.omnisearch.crawl.dto.CrawlRequest;
import com.omnisearch.crawl.service.CrawlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawl")
public class CrawlController {

    private final CrawlerService crawlerService;

    // Manual Constructor for Spring Dependency Injection (bypasses Lombok)
    public CrawlController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @PostMapping
    public ResponseEntity<String> startCrawl(@RequestBody CrawlRequest request){
        crawlerService.startCrawling(request.getSeedUrl(), request.getMaxDepth());
        return ResponseEntity.ok("Crawling started for: "+ request.getSeedUrl());
    }
}
