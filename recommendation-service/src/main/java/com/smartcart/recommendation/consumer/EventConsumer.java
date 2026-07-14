package com.smartcart.recommendation.consumer;

import com.smartcart.recommendation.dto.EventDtos.*;
import com.smartcart.recommendation.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class EventConsumer {
    private final UserActivityTracker activityTracker;
    private final RecommendationService recommendationService;

    @KafkaListener(topics = "smartcart.orders", groupId = "recommendation-group", properties = {"spring.json.value.default.type=com.smartcart.recommendation.dto.EventDtos$OrderCreatedEvent"})
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received ORDER_CREATED: orderId={}", event.getOrderId());
        event.getItems().forEach(i -> activityTracker.addPurchase(event.getUserId(), i.getProductName(), i.getCategory()));
        recommendationService.invalidateCache(event.getUserId());
    }

    @KafkaListener(topics = "smartcart.wishlist", groupId = "recommendation-group", properties = {"spring.json.value.default.type=com.smartcart.recommendation.dto.EventDtos$WishlistEvent"})
    public void handleWishlistEvent(WishlistEvent event) {
        log.info("Received WISHLIST_{}: userId={}", event.getAction(), event.getUserId());
        if ("ADDED".equals(event.getAction())) activityTracker.addWishlistItem(event.getUserId(), event.getProductName(), event.getCategory(), "N/A");
        else if ("REMOVED".equals(event.getAction())) activityTracker.removeWishlistItem(event.getUserId(), event.getProductName());
        recommendationService.invalidateCache(event.getUserId());
    }
}
