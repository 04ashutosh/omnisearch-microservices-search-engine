package com.omnisearch.indexer.repository;

import com.omnisearch.indexer.entity.WebDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebDocumentRepository extends ElasticsearchRepository<WebDocument, String> {

}
