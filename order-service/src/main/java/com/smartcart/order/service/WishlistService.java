package com.smartcart.order.service;

import com.smartcart.order.dto.ProductDtos.ProductResponse;
import com.smartcart.order.entity.Product;
import com.smartcart.order.entity.User;
import com.smartcart.order.entity.WishlistItem;
import com.smartcart.order.event.EventPublisher;
import com.smartcart.order.event.OrderEvents;
import com.smartcart.order.exception.DuplicateResourceException;
import com.smartcart.order.exception.ResourceNotFoundException;
import com.smartcart.order.repository.ProductRepository;
import com.smartcart.order.repository.UserRepository;
import com.smartcart.order.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public void addToWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new DuplicateResourceException("Product already in wishlist");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        WishlistItem item = WishlistItem.builder().user(user).product(product).build();
        wishlistRepository.save(item);
        log.info("Product {} added to wishlist for user {}", productId, userId);

        eventPublisher.publishWishlistEvent(OrderEvents.WishlistEvent.builder()
                .userId(userId).userEmail(user.getEmail())
                .productId(productId).productName(product.getName())
                .category(product.getCategory()).action("ADDED")
                .timestamp(LocalDateTime.now()).build());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(item -> mapToProductResponse(item.getProduct()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException("Product not in wishlist");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        log.info("Product {} removed from wishlist for user {}", productId, userId);

        eventPublisher.publishWishlistEvent(OrderEvents.WishlistEvent.builder()
                .userId(userId).userEmail(user.getEmail())
                .productId(productId).productName(product.getName())
                .category(product.getCategory()).action("REMOVED")
                .timestamp(LocalDateTime.now()).build());
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId()).name(product.getName())
                .description(product.getDescription()).price(product.getPrice())
                .category(product.getCategory()).stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl()).active(product.getActive())
                .createdAt(product.getCreatedAt()).build();
    }
}
