package com.omnisearch.crawl.dto;

public class CrawlRequest {
    private String seedUrl;
    private int maxDepth = 2; // Default to 2 so we don't crawl the whole internet

    public CrawlRequest() {}

    public String getSeedUrl() { return seedUrl; }
    public void setSeedUrl(String seedUrl) { this.seedUrl = seedUrl; }
    
    public int getMaxDepth() { return maxDepth; }
    public void setMaxDepth(int maxDepth) { this.maxDepth = maxDepth; }
}
