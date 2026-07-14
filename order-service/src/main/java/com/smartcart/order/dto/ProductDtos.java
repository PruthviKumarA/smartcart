package com.smartcart.order.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDtos {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateProductRequest {
        @NotBlank private String name;
        private String description;
        @NotNull @DecimalMin("0.01") private BigDecimal price;
        @NotBlank private String category;
        @NotNull @Min(0) private Integer stockQuantity;
        private String imageUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private Integer stockQuantity;
        private String imageUrl;
        private Boolean active;
        private LocalDateTime createdAt;
    }
}
