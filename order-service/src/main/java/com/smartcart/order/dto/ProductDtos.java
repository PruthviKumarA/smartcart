package com.smartcart.order.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateProductRequest {
        @NotBlank(message = "Product name is required")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        @NotBlank(message = "Category is required")
        private String category;

        @NotNull @Min(0)
        private Integer stockQuantity;

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
