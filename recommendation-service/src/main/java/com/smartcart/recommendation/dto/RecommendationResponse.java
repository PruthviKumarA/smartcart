package com.smartcart.recommendation.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecommendationResponse {
    private Long userId; private List<ProductSuggestion> suggestions; private String reasoning; private LocalDateTime generatedAt; private String source;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductSuggestion { private String productName; private String category; private String reason; private String priceRange; }
}
