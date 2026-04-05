package com.smartcart.order.controller;

import com.smartcart.order.dto.ProductDtos.ProductResponse;
import com.smartcart.order.security.UserPrincipal;
import com.smartcart.order.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addToWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId) {
        wishlistService.addToWishlist(principal.getUserId(), productId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getWishlist(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(wishlistService.getWishlist(principal.getUserId()));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId) {
        wishlistService.removeFromWishlist(principal.getUserId(), productId);
        return ResponseEntity.noContent().build();
    }
}
