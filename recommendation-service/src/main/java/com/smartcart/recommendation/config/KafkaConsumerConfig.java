package com.smartcart.recommendation.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import java.util.*;

@Configuration @Slf4j
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}") private String bootstrapServers;

    @Bean public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> c = new HashMap<>();
        c.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        c.put(ConsumerConfig.GROUP_ID_CONFIG, "recommendation-group");
        c.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        c.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        c.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        c.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(c);
    }

    @Bean public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(consumerFactory());
        f.setCommonErrorHandler(new DefaultErrorHandler((r, ex) -> log.error("Failed: {}", ex.getMessage()), new FixedBackOff(1000L, 3L)));
        f.setConcurrency(3);
        return f;
    }
}
