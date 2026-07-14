package com.smartcart.order.controller;

import com.smartcart.order.dto.OrderDtos.*;
import com.smartcart.order.security.UserPrincipal;
import com.smartcart.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/orders") @RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal UserPrincipal p, @Valid @RequestBody CreateOrderRequest req) { return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(p.getUserId(), req)); }
    @GetMapping public ResponseEntity<Page<OrderResponse>> myOrders(@AuthenticationPrincipal UserPrincipal p, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) { return ResponseEntity.ok(orderService.getUserOrders(p.getUserId(), page, size)); }
    @GetMapping("/{orderId}") public ResponseEntity<OrderResponse> getOrder(@AuthenticationPrincipal UserPrincipal p, @PathVariable Long orderId) { return ResponseEntity.ok(orderService.getOrderById(orderId, p.getUserId())); }
    @PatchMapping("/{orderId}/status") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long orderId, @RequestParam String status) { return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status)); }
    @GetMapping("/analytics") public ResponseEntity<Map<String, Object>> analytics(@AuthenticationPrincipal UserPrincipal p) { return ResponseEntity.ok(orderService.getOrderAnalytics(p.getUserId())); }
}
