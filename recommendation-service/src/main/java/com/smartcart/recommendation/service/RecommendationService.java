package com.smartcart.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcart.recommendation.client.ClaudeApiClient;
import com.smartcart.recommendation.dto.RecommendationResponse;
import com.smartcart.recommendation.dto.RecommendationResponse.ProductSuggestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class RecommendationService {
    private final ClaudeApiClient claudeApiClient;
    private final UserActivityTracker activityTracker;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    @Value("${recommendation.cache.ttl-minutes:60}") private int cacheTtlMinutes;
    private static final String CACHE_KEY_PREFIX = "recommendations:user:";

    public RecommendationResponse getRecommendations(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) { RecommendationResponse r = objectMapper.convertValue(cached, RecommendationResponse.class); r.setSource("CACHED"); return r; }
        } catch (Exception e) { log.warn("Redis read failed"); }

        List<Map<String, String>> wishlist = activityTracker.getWishlist(userId);
        List<Map<String, String>> purchases = activityTracker.getPurchaseHistory(userId);
        List<ProductSuggestion> suggestions = claudeApiClient.getRecommendations(wishlist, purchases);

        RecommendationResponse response = RecommendationResponse.builder().userId(userId).suggestions(suggestions)
                .reasoning("Based on " + wishlist.size() + " wishlist items and " + purchases.size() + " purchases")
                .generatedAt(LocalDateTime.now()).source("AI").build();

        try { redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(cacheTtlMinutes)); } catch (Exception e) { log.warn("Redis write failed"); }
        return response;
    }

    public void invalidateCache(Long userId) { try { redisTemplate.delete(CACHE_KEY_PREFIX + userId); } catch (Exception e) {} }

    @Scheduled(cron = "${recommendation.refresh.cron:0 0 */6 * * *}")
    public void refreshRecommendationsForActiveUsers() {
        Set<Long> users = activityTracker.getAllTrackedUserIds();
        log.info("Refreshing recommendations for {} users", users.size());
        users.forEach(id -> { try { invalidateCache(id); getRecommendations(id); } catch (Exception e) { log.error("Refresh failed for user {}", id); } });
    }
}
