package com.smartcart.recommendation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service @Slf4j
public class UserActivityTracker {
    private final Map<Long, List<Map<String, String>>> userWishlist = new ConcurrentHashMap<>();
    private final Map<Long, List<Map<String, String>>> userPurchases = new ConcurrentHashMap<>();

    public void addWishlistItem(Long userId, String name, String category, String price) {
        userWishlist.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>())).add(Map.of("name", name, "category", category, "price", price));
    }
    public void removeWishlistItem(Long userId, String name) {
        List<Map<String, String>> items = userWishlist.get(userId);
        if (items != null) items.removeIf(i -> i.get("name").equals(name));
    }
    public void addPurchase(Long userId, String name, String category) {
        userPurchases.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>())).add(Map.of("name", name, "category", category));
    }
    public List<Map<String, String>> getWishlist(Long userId) { return userWishlist.getOrDefault(userId, List.of()); }
    public List<Map<String, String>> getPurchaseHistory(Long userId) { return userPurchases.getOrDefault(userId, List.of()); }
    public Set<Long> getAllTrackedUserIds() { Set<Long> all = new HashSet<>(); all.addAll(userWishlist.keySet()); all.addAll(userPurchases.keySet()); return all; }
}
