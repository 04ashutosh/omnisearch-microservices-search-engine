package com.omnisearch.search.controller;

import com.omnisearch.search.dto.SearchResult;
import com.omnisearch.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    // Example: GET http://localhost:8080/search?q=microservices
    @GetMapping
    public ResponseEntity<List<SearchResult>> searchQueries(@RequestParam("q") String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<SearchResult> results = searchService.search(query);
        return ResponseEntity.ok(results);
    }

    // Example: GET http://localhost:8080/search/suggest?q=sprin
    @GetMapping("/suggest")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam("q") String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return ResponseEntity.ok(List.of()); // Send an empty dropdown if they haven't typed anything
        }
        
        List<String> suggestions = searchService.getSuggestions(prefix);
        return ResponseEntity.ok(suggestions);
    }
}
