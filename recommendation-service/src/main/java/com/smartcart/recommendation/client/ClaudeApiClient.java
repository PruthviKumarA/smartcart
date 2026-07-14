package com.smartcart.recommendation.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcart.recommendation.dto.RecommendationResponse.ProductSuggestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

@Component @Slf4j
public class ClaudeApiClient {
    private final WebClient webClient;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaudeApiClient(@Value("${claude.api.key}") String apiKey, @Value("${claude.api.url}") String apiUrl, @Value("${claude.api.model}") String model) {
        this.model = model;
        this.webClient = WebClient.builder().baseUrl(apiUrl).defaultHeader("x-api-key", apiKey).defaultHeader("anthropic-version", "2023-06-01").defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
    }

    public List<ProductSuggestion> getRecommendations(List<Map<String, String>> wishlist, List<Map<String, String>> purchases) {
        try {
            StringBuilder sb = new StringBuilder("You are a product recommendation engine.\n\nWishlist:\n");
            wishlist.forEach(i -> sb.append("- ").append(i.get("name")).append(" (").append(i.get("category")).append(")\n"));
            sb.append("\nPurchases:\n");
            purchases.forEach(i -> sb.append("- ").append(i.get("name")).append(" (").append(i.get("category")).append(")\n"));
            sb.append("\nSuggest 5 products as JSON array: [{\"productName\":\"\",\"category\":\"\",\"reason\":\"\",\"priceRange\":\"\"}]");

            String response = webClient.post().bodyValue(Map.of("model", model, "max_tokens", 1024, "messages", List.of(Map.of("role", "user", "content", sb.toString())))).retrieve().bodyToMono(String.class).block();
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("content").get(0).path("text").asText().replaceAll("```json|```", "").trim();
            List<ProductSuggestion> result = new ArrayList<>();
            for (JsonNode n : objectMapper.readTree(content))
                result.add(ProductSuggestion.builder().productName(n.path("productName").asText()).category(n.path("category").asText()).reason(n.path("reason").asText()).priceRange(n.path("priceRange").asText()).build());
            return result;
        } catch (Exception e) {
            log.error("Claude API failed: {}", e.getMessage());
            return wishlist.stream().map(i -> ProductSuggestion.builder().productName("Top " + i.get("category")).category(i.get("category")).reason("Based on your interest").priceRange("Varies").build()).toList();
        }
    }
}
