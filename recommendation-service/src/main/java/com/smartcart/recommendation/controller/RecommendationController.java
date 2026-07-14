package com.smartcart.recommendation.controller;

import com.smartcart.recommendation.dto.RecommendationResponse;
import com.smartcart.recommendation.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/recommendations") @RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final UserActivityTracker activityTracker;

    @GetMapping("/user/{userId}") public ResponseEntity<RecommendationResponse> get(@PathVariable Long userId) { return ResponseEntity.ok(recommendationService.getRecommendations(userId)); }
    @GetMapping("/activity/{userId}") public ResponseEntity<Map<String, Object>> activity(@PathVariable Long userId) { return ResponseEntity.ok(Map.of("userId", userId, "wishlist", activityTracker.getWishlist(userId), "purchases", activityTracker.getPurchaseHistory(userId))); }
    @PostMapping("/refresh/{userId}") public ResponseEntity<RecommendationResponse> refresh(@PathVariable Long userId) { recommendationService.invalidateCache(userId); return ResponseEntity.ok(recommendationService.getRecommendations(userId)); }
    @GetMapping("/health") public ResponseEntity<Map<String, Object>> health() { return ResponseEntity.ok(Map.of("service", "recommendation-service", "status", "UP", "trackedUsers", activityTracker.getAllTrackedUserIds().size())); }
}
