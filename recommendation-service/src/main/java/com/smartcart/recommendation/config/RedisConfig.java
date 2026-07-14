package com.smartcart.recommendation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration @Slf4j
public class RedisConfig {
    @Bean public ObjectMapper objectMapper() { ObjectMapper m = new ObjectMapper(); m.registerModule(new JavaTimeModule()); m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); return m; }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Object> t = new RedisTemplate<>();
        t.setConnectionFactory(cf); t.setKeySerializer(new StringRedisSerializer()); t.setValueSerializer(new GenericJackson2JsonRedisSerializer()); t.afterPropertiesSet();
        try { cf.getConnection().ping(); log.info("Redis connected"); } catch (Exception e) { log.warn("Redis not available"); }
        return t;
    }
}
