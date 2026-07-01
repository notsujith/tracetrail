package com.tracetrail.ingestion.buffer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "tracetrail.ingestion.buffer", havingValue = "kafka")
public class KafkaTopicConfig {

    @Bean
    public NewTopic spansTopic(
            @Value("${tracetrail.ingestion.kafka.topic:tt.spans}") String topic,
            @Value("${tracetrail.ingestion.kafka.partitions:3}") int partitions) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(1).build();
    }
}