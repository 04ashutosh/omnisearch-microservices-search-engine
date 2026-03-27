package com.omnisearch.search.repository;

import com.omnisearch.search.entity.WebDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchRepository extends ElasticsearchRepository<WebDocument, String> {
    
    // Spring Data Magic! 
    List<WebDocument> findByTitleMatchesOrContentMatches(String titleQuery, String contentQuery);
}