package com.smartcart.recommendation.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EventDtos {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderCreatedEvent implements Serializable {
        private Long orderId; private Long userId; private String userEmail; private BigDecimal totalAmount;
        private List<OrderItemEvent> items; private String status; private LocalDateTime createdAt;
    }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemEvent implements Serializable {
        private Long productId; private String productName; private String category; private Integer quantity; private BigDecimal price;
    }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WishlistEvent implements Serializable {
        private Long userId; private String userEmail; private Long productId; private String productName; private String category; private String action; private LocalDateTime timestamp;
    }
}
