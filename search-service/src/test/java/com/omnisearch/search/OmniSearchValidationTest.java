package com.omnisearch.search;

import com.omnisearch.common.PageCrawledEvent;
import com.omnisearch.search.dto.SearchResult;
import com.omnisearch.search.entity.WebDocument;
import com.omnisearch.search.controller.SearchController;
import com.omnisearch.search.service.SearchService;
import com.omnisearch.search.repository.SearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.mockito.Mockito;
import java.util.Collections;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class OmniSearchValidationTest {

    private WebDocument doc(String id, String title, String content) {
        WebDocument w = new WebDocument();
        w.setId(id);
        w.setTitle(title);
        w.setContent(content);
        return w;
    }

    // --- PageCrawledEvent Tests (6) ---
    @Test void testEventUrl() { PageCrawledEvent e = new PageCrawledEvent(); e.setUrl("http://a.com"); assertEquals("http://a.com", e.getUrl()); }
    @Test void testEventTitle() { PageCrawledEvent e = new PageCrawledEvent(); e.setTitle("Title"); assertEquals("Title", e.getTitle()); }
    @Test void testEventContent() { PageCrawledEvent e = new PageCrawledEvent(); e.setContent("Cont"); assertEquals("Cont", e.getContent()); }
    @Test void testEventConstructorUrl() { PageCrawledEvent e = new PageCrawledEvent("http://x.com", "T", "C"); assertEquals("http://x.com", e.getUrl()); }
    @Test void testEventConstructorTitle() { PageCrawledEvent e = new PageCrawledEvent("http://x.com", "T", "C"); assertEquals("T", e.getTitle()); }
    @Test void testEventConstructorContent() { PageCrawledEvent e = new PageCrawledEvent("x", "y", "Z"); assertEquals("Z", e.getContent()); }

    // --- WebDocument Tests (7) ---
    @Test void testDocId() { WebDocument w = new WebDocument(); w.setId("1"); assertEquals("1", w.getId()); }
    @Test void testDocTitle() { WebDocument w = new WebDocument(); w.setTitle("T"); assertEquals("T", w.getTitle()); }
    @Test void testDocContent() { WebDocument w = new WebDocument(); w.setContent("C"); assertEquals("C", w.getContent()); }
    @Test void testDocConstructorId() { WebDocument w = doc("1", "2", "3"); assertEquals("1", w.getId()); }
    @Test void testDocConstructorTitle() { WebDocument w = doc("1", "2", "3"); assertEquals("2", w.getTitle()); }
    @Test void testDocConstructorContent() { WebDocument w = doc("1", "2", "3"); assertEquals("3", w.getContent()); }
    @Test void testDocConstructorEmpty() { WebDocument w = new WebDocument(); assertNull(w.getId()); assertNull(w.getTitle()); assertNull(w.getContent()); }

    // --- SearchResult DTO Tests (2) ---
    @Test void testSearchResultConstructor() { SearchResult sr = new SearchResult("u", "t", "s"); assertEquals("u", sr.getUrl()); }
    @Test void testSearchResultGetters() { SearchResult sr = new SearchResult("u", "t", "s"); assertEquals("t", sr.getTitle()); assertEquals("s", sr.getSnippet()); }

    // --- SearchController Tests (4) ---
    @Test void testSearchControllerNullQuery() { SearchController c = new SearchController(null); assertEquals(400, c.searchQueries(null).getStatusCode().value()); }
    @Test void testSearchControllerEmptyQuery() { SearchController c = new SearchController(null); assertEquals(400, c.searchQueries("").getStatusCode().value()); }
    @Test void testSearchControllerBlankQuery() { SearchController c = new SearchController(null); assertEquals(400, c.searchQueries("   ").getStatusCode().value()); }
    @Test void testSearchControllerValidQuery() { 
        SearchService mockService = Mockito.mock(SearchService.class);
        Mockito.when(mockService.search("Java")).thenReturn(Collections.emptyList());
        SearchController c = new SearchController(mockService); 
        assertEquals(200, c.searchQueries("Java").getStatusCode().value()); 
    }

    // --- SearchService Mapping Tests (2) ---
    @Test void testServiceFormat() {
        org.springframework.data.elasticsearch.core.ElasticsearchOperations mockOps = Mockito.mock(org.springframework.data.elasticsearch.core.ElasticsearchOperations.class);
        SearchService service = new SearchService(mockOps);
        WebDocument dummy = doc("id", "title", "content");
        
        org.springframework.data.elasticsearch.core.SearchHits<WebDocument> mockHits = Mockito.mock(org.springframework.data.elasticsearch.core.SearchHits.class);
        org.springframework.data.elasticsearch.core.SearchHit<WebDocument> mockHit = Mockito.mock(org.springframework.data.elasticsearch.core.SearchHit.class);
        Mockito.when(mockHit.getContent()).thenReturn(dummy);
        Mockito.when(mockHit.getHighlightFields()).thenReturn(Collections.emptyMap());
        Mockito.when(mockHits.getSearchHits()).thenReturn(Collections.singletonList(mockHit));
        Mockito.when(mockOps.search(Mockito.any(org.springframework.data.elasticsearch.core.query.Query.class), Mockito.eq(WebDocument.class))).thenReturn(mockHits);
        
        assertEquals(1, service.search("query").size());
    }
    
    @Test void testServiceMultiFormat() {
        org.springframework.data.elasticsearch.core.ElasticsearchOperations mockOps = Mockito.mock(org.springframework.data.elasticsearch.core.ElasticsearchOperations.class);
        SearchService service = new SearchService(mockOps);
        
        org.springframework.data.elasticsearch.core.SearchHits<WebDocument> mockHits = Mockito.mock(org.springframework.data.elasticsearch.core.SearchHits.class);
        org.springframework.data.elasticsearch.core.SearchHit<WebDocument> mockHit1 = Mockito.mock(org.springframework.data.elasticsearch.core.SearchHit.class);
        org.springframework.data.elasticsearch.core.SearchHit<WebDocument> mockHit2 = Mockito.mock(org.springframework.data.elasticsearch.core.SearchHit.class);
        Mockito.when(mockHit1.getContent()).thenReturn(doc("1", "2", "3"));
        Mockito.when(mockHit1.getHighlightFields()).thenReturn(Collections.emptyMap());
        Mockito.when(mockHit2.getContent()).thenReturn(doc("4", "5", "6"));
        Mockito.when(mockHit2.getHighlightFields()).thenReturn(Collections.emptyMap());
        Mockito.when(mockHits.getSearchHits()).thenReturn(Arrays.asList(mockHit1, mockHit2));
        Mockito.when(mockOps.search(Mockito.any(org.springframework.data.elasticsearch.core.query.Query.class), Mockito.eq(WebDocument.class))).thenReturn(mockHits);
        
        assertEquals(2, service.search("query").size());
    }
}
