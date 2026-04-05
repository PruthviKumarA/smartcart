package com.smartcart.order.event;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Events published to Kafka topics.
 * 
 * INTERVIEW QUESTION: "How do your microservices communicate?"
 * ANSWER: "Asynchronously via Kafka events. When a user places an order or 
 * updates their wishlist, the Order Service publishes an event to a Kafka topic. 
 * The Recommendation Service consumes these events to update personalized 
 * product suggestions. This decouples the services — if Recommendation Service 
 * is down, orders still work. Events are persisted in Kafka, so the consumer 
 * processes them when it comes back up."
 */
public class OrderEvents {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderCreatedEvent implements Serializable {
        private Long orderId;
        private Long userId;
        private String userEmail;
        private BigDecimal totalAmount;
        private List<OrderItemEvent> items;
        private String status;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemEvent implements Serializable {
        private Long productId;
        private String productName;
        private String category;
        private Integer quantity;
        private BigDecimal price;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WishlistEvent implements Serializable {
        private Long userId;
        private String userEmail;
        private Long productId;
        private String productName;
        private String category;
        private String action;  // "ADDED" or "REMOVED"
        private LocalDateTime timestamp;
    }
}
