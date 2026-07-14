package com.smartcart.order.service;

import com.smartcart.order.dto.ProductDtos.ProductResponse;
import com.smartcart.order.entity.*;
import com.smartcart.order.event.*;
import com.smartcart.order.exception.*;
import com.smartcart.order.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public void addToWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) throw new DuplicateResourceException("Already in wishlist");
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        wishlistRepository.save(WishlistItem.builder().user(user).product(product).build());
        eventPublisher.publishWishlistEvent(OrderEvents.WishlistEvent.builder().userId(userId).userEmail(user.getEmail())
                .productId(productId).productName(product.getName()).category(product.getCategory()).action("ADDED").timestamp(LocalDateTime.now()).build());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId).stream().map(i -> ProductResponse.builder().id(i.getProduct().getId())
                .name(i.getProduct().getName()).description(i.getProduct().getDescription()).price(i.getProduct().getPrice())
                .category(i.getProduct().getCategory()).stockQuantity(i.getProduct().getStockQuantity()).active(i.getProduct().getActive()).createdAt(i.getProduct().getCreatedAt()).build()).collect(Collectors.toList());
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) throw new ResourceNotFoundException("Not in wishlist");
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        eventPublisher.publishWishlistEvent(OrderEvents.WishlistEvent.builder().userId(userId).userEmail(user.getEmail())
                .productId(productId).productName(product.getName()).category(product.getCategory()).action("REMOVED").timestamp(LocalDateTime.now()).build());
    }
}
