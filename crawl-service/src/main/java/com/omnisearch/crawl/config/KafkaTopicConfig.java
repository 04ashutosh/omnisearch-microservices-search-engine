package com.omnisearch.crawl.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic webContentTopic(){
        return TopicBuilder.name("web-content-topic")
                .partitions(3) //Split the queue into 3 parallel highways
                .replicas(1)
                .build();
    }
}
