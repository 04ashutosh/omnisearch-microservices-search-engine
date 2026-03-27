package com.omnisearch.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.omnisearch.search.dto.SearchResult;
import com.omnisearch.search.entity.WebDocument;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final ElasticsearchOperations elasticsearchOperations;

    public SearchService(ElasticsearchOperations elasticsearchOperations){
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public List<SearchResult> search(String queryKeyword){
        /*
         * 1. THE RANKING ENGINE (Custom Scoring)
         * We boost the "title" field multiplier by 3.0x so that hits in the
         * title are ranked exponentially higher than hits in the body content!
         */
        var multiMatchQuery = QueryBuilders.multiMatch()
                .query(queryKeyword)
                .fields("title^3.0","content^1.0")
                .build()._toQuery();

        /*
         * 2. THE HIGHLIGHTING ENGINE
         * We tell Elasticsearch to extract surrounding sentence snippets
         * and wrap the keyword in HTML <em> tags.
         */
        Highlight highlight = new Highlight(List.of(
                new HighlightField("content")
        ));

        //3. Assemble the NativeQuery
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(multiMatchQuery)
                .withHighlightQuery(new HighlightQuery(highlight,null))
                .build();

        //4. Execute the blazing fast query to grab the Top ranked Results
        SearchHits<WebDocument> searchHits = elasticsearchOperations.search(nativeQuery,WebDocument.class);

        // 5. Build our robust Response by looping over the Hits
        return searchHits.getSearchHits().stream().map(hit -> {
            WebDocument doc = hit.getContent();
            String finalSnippet = "";
            // If Elasticsearch perfectly matched the keyword, it generates a highlighted snippet for us!
            // Example: "Learn how to deploy <em class='hlt1'>spring boot</em> applications"
            if (!hit.getHighlightFields().isEmpty() && hit.getHighlightFields().containsKey("content")) {
                finalSnippet = hit.getHighlightFields().get("content").get(0);
            } else {
                // Formatting fallback
                finalSnippet = doc.getContent() != null && doc.getContent().length() > 150 ?
                        doc.getContent().substring(0, 150) + "..." : doc.getContent();
            }
            return new SearchResult(
                    doc.getId(),
                    doc.getTitle() != null ? doc.getTitle() : "No Title",
                    finalSnippet
            );
        }).collect(Collectors.toList());
    }

    public List<String> getSuggestions(String partialPrefix) {
        // Use a "Match Phrase Prefix" query to find titles that start with the user's keystrokes
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(QueryBuilders.matchPhrasePrefix()
                        .field("title")
                        .query(partialPrefix)
                        .build()._toQuery())
                .withMaxResults(5) // Limit to 5 fast suggestions!
                .build();

        // Perform the scan on the Database
        SearchHits<WebDocument> hits = elasticsearchOperations.search(nativeQuery, WebDocument.class);

        // Map the heavy results into a simple list of 5 beautifully formatted String Titles
        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getTitle())
                .distinct() // Remove duplicate title suggestions
                .collect(Collectors.toList());
    }
}