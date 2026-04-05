package com.smartcart.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer Configuration.
 * 
 * INTERVIEW QUESTION: "Explain your Kafka setup."
 * ANSWER: "I configure a KafkaTemplate with StringSerializer for keys and 
 * JsonSerializer for values. I create two topics — one for order events 
 * and one for wishlist events. Each topic has 3 partitions for parallel 
 * consumption. I use the userId as the message key, which ensures all 
 * events for the same user go to the same partition, maintaining order 
 * per user. The producer is configured with acks=all for durability."
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");  // wait for all replicas
        config.put(ProducerConfig.RETRIES_CONFIG, 3);     // retry on failure
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Auto-create topics if they don't exist
    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder.name("smartcart.orders")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic wishlistTopic() {
        return TopicBuilder.name("smartcart.wishlist")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
