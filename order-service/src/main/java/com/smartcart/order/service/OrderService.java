package com.smartcart.order.service;

import com.smartcart.order.dto.OrderDtos.*;
import com.smartcart.order.entity.*;
import com.smartcart.order.event.EventPublisher;
import com.smartcart.order.event.OrderEvents;
import com.smartcart.order.exception.BadRequestException;
import com.smartcart.order.exception.ResourceNotFoundException;
import com.smartcart.order.repository.OrderRepository;
import com.smartcart.order.repository.ProductRepository;
import com.smartcart.order.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Long> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        Map<Long, Product> productMap = productRepository.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .status(Order.OrderStatus.PENDING)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productMap.get(itemReq.getProductId());
            if (product == null) {
                throw new ResourceNotFoundException("Product not found: " + itemReq.getProductId());
            }
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
            order.addItem(item);

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        Order saved = orderRepository.save(order);
        productRepository.saveAll(productMap.values());

        log.info("Order created: id={}, userId={}, total={}", saved.getId(), userId, totalAmount);

        // Publish Kafka event
        OrderEvents.OrderCreatedEvent event = OrderEvents.OrderCreatedEvent.builder()
                .orderId(saved.getId())
                .userId(userId)
                .userEmail(user.getEmail())
                .totalAmount(totalAmount)
                .status(saved.getStatus().name())
                .createdAt(LocalDateTime.now())
                .items(saved.getItems().stream()
                        .map(item -> OrderEvents.OrderItemEvent.builder()
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .category(item.getProduct().getCategory())
                                .quantity(item.getQuantity())
                                .price(item.getPriceAtPurchase())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        eventPublisher.publishOrderCreated(event);
        processOrderAsync(saved.getId());

        return mapToResponse(saved);
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> processOrderAsync(Long orderId) {
        log.info("Async processing order {} on thread: {}", orderId, Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
            log.info("Email notification sent for order {}", orderId);
            Thread.sleep(500);
            log.info("Analytics updated for order {}", orderId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Async processing interrupted for order {}", orderId);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, int page, int size) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        Order updated = orderRepository.save(order);
        log.info("Order {} status updated to {}", orderId, status);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOrderAnalytics(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(0, 1000)).getContent();

        BigDecimal totalSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()));

        double avgOrderValue = orders.stream()
                .mapToDouble(o -> o.getTotalAmount().doubleValue())
                .average().orElse(0.0);

        return Map.of(
                "totalOrders", orders.size(),
                "totalSpent", totalSpent,
                "averageOrderValue", avgOrderValue,
                "ordersByStatus", ordersByStatus
        );
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userName(order.getUser().getName())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
