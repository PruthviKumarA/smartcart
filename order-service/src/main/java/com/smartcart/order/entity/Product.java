package com.smartcart.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "products", indexes = {
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_price", columnList = "price")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(length = 1000)
    private String description;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private String category;
    @Column(nullable = false)
    private Integer stockQuantity;
    private String imageUrl;
    @Column(nullable = false)
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); if (active == null) active = true; }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
