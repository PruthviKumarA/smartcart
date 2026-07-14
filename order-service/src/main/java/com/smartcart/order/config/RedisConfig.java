package com.smartcart.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import java.time.Duration;

@Configuration @EnableCaching @Slf4j
public class RedisConfig {
    @Bean @Primary
    public CacheManager cacheManager(RedisConnectionFactory cf) {
        try {
            cf.getConnection().ping();
            log.info("Redis connected - using Redis cache");
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                    .disableCachingNullValues();
            return RedisCacheManager.builder(cf).cacheDefaults(config)
                    .withCacheConfiguration("products", config.entryTtl(Duration.ofMinutes(30)))
                    .withCacheConfiguration("productListings", config.entryTtl(Duration.ofMinutes(10)))
                    .build();
        } catch (Exception e) {
            log.warn("Redis not available - falling back to in-memory cache");
            return new ConcurrentMapCacheManager("products", "productListings");
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Object> t = new RedisTemplate<>();
        t.setConnectionFactory(cf);
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        t.afterPropertiesSet();
        return t;
    }
}
