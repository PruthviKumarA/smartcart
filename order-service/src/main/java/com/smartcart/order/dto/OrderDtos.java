package com.smartcart.order.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateOrderRequest {
        @NotBlank(message = "Shipping address is required")
        private String shippingAddress;

        @NotEmpty(message = "Order must have at least one item")
        private List<OrderItemRequest> items;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull private Long productId;
        @NotNull @Min(1) private Integer quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderResponse {
        private Long id;
        private String userName;
        private List<OrderItemResponse> items;
        private BigDecimal totalAmount;
        private String status;
        private String shippingAddress;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal priceAtPurchase;
    }
}
