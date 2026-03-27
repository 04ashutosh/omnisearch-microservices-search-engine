package com.omnisearch.indexer.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "web_pages") // the "table" name in Elasticsearch
public class WebDocument {
    @Id
    private String id; //we use the url as the unique ID so if we crawl the same site twice, we just overwrite the old version instead of duplicating it!

    @Field(type = FieldType.Text, searchAnalyzer = "standard", analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, searchAnalyzer = "standard", analyzer = "standard")
    private String content; // Elasticsearch will automatically tokenize this (split into words) and remove stopwords!

    //Standard Constructors
    public WebDocument(){}

    public WebDocument(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public  String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}
}
