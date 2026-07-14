package com.smartcart.order.controller;

import com.smartcart.order.dto.ProductDtos.ProductResponse;
import com.smartcart.order.security.UserPrincipal;
import com.smartcart.order.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/wishlist") @RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    @PostMapping("/{productId}") public ResponseEntity<Void> add(@AuthenticationPrincipal UserPrincipal p, @PathVariable Long productId) { wishlistService.addToWishlist(p.getUserId(), productId); return ResponseEntity.status(HttpStatus.CREATED).build(); }
    @GetMapping public ResponseEntity<List<ProductResponse>> get(@AuthenticationPrincipal UserPrincipal p) { return ResponseEntity.ok(wishlistService.getWishlist(p.getUserId())); }
    @DeleteMapping("/{productId}") public ResponseEntity<Void> remove(@AuthenticationPrincipal UserPrincipal p, @PathVariable Long productId) { wishlistService.removeFromWishlist(p.getUserId(), productId); return ResponseEntity.noContent().build(); }
}
