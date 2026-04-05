package com.smartcart.order.service;

import com.smartcart.order.dto.OrderDtos.*;
import com.smartcart.order.entity.*;
import com.smartcart.order.event.EventPublisher;
import com.smartcart.order.exception.BadRequestException;
import com.smartcart.order.exception.ResourceNotFoundException;
import com.smartcart.order.repository.OrderRepository;
import com.smartcart.order.repository.ProductRepository;
import com.smartcart.order.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private User sampleUser;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L).name("Pruthvi").email("pruthvi@test.com")
                .password("encoded").role(User.Role.CUSTOMER).build();

        sampleProduct = Product.builder()
                .id(1L).name("iPhone 15").price(new BigDecimal("134900.00"))
                .category("Electronics").stockQuantity(50).active(true)
                .createdAt(LocalDateTime.now()).build();
    }

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {

        @Test
        @DisplayName("should create order successfully with valid items")
        void shouldCreateOrder() {
            // Arrange
            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddress("Bengaluru, KA");
            OrderItemRequest itemReq = new OrderItemRequest();
            itemReq.setProductId(1L);
            itemReq.setQuantity(2);
            request.setItems(List.of(itemReq));

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(productRepository.findByIdIn(List.of(1L))).thenReturn(List.of(sampleProduct));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(productRepository.saveAll(any())).thenReturn(List.of(sampleProduct));
            doNothing().when(eventPublisher).publishOrderCreated(any());

            // Act
            OrderResponse response = orderService.createOrder(1L, request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("269800.00")); // 134900 * 2
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getShippingAddress()).isEqualTo("Bengaluru, KA");

            // Verify stock was reduced
            assertThat(sampleProduct.getStockQuantity()).isEqualTo(48); // 50 - 2

            // Verify Kafka event was published
            verify(eventPublisher).publishOrderCreated(any());
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrow_whenUserNotFound() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setItems(List.of());

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("should throw when product not found")
        void shouldThrow_whenProductNotFound() {
            CreateOrderRequest request = new CreateOrderRequest();
            OrderItemRequest itemReq = new OrderItemRequest();
            itemReq.setProductId(999L);
            itemReq.setQuantity(1);
            request.setItems(List.of(itemReq));
            request.setShippingAddress("Bengaluru");

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(productRepository.findByIdIn(List.of(999L))).thenReturn(List.of());

            assertThatThrownBy(() -> orderService.createOrder(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("should throw when insufficient stock")
        void shouldThrow_whenInsufficientStock() {
            sampleProduct.setStockQuantity(1); // only 1 in stock

            CreateOrderRequest request = new CreateOrderRequest();
            OrderItemRequest itemReq = new OrderItemRequest();
            itemReq.setProductId(1L);
            itemReq.setQuantity(5); // ordering 5
            request.setItems(List.of(itemReq));
            request.setShippingAddress("Bengaluru");

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(productRepository.findByIdIn(List.of(1L))).thenReturn(List.of(sampleProduct));

            assertThatThrownBy(() -> orderService.createOrder(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Insufficient stock");
        }
    }

    @Nested
    @DisplayName("getOrderById")
    class GetOrderById {

        @Test
        @DisplayName("should return order when it belongs to user")
        void shouldReturnOrder() {
            Order order = Order.builder()
                    .id(1L).user(sampleUser).totalAmount(new BigDecimal("134900.00"))
                    .status(Order.OrderStatus.PENDING).shippingAddress("Bengaluru")
                    .createdAt(LocalDateTime.now()).items(List.of()).build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            OrderResponse response = orderService.getOrderById(1L, 1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("should throw when order does not belong to user")
        void shouldThrow_whenOrderNotBelongToUser() {
            Order order = Order.builder()
                    .id(1L).user(sampleUser).totalAmount(new BigDecimal("134900.00"))
                    .status(Order.OrderStatus.PENDING).items(List.of()).build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.getOrderById(1L, 999L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Order does not belong to this user");
        }
    }

    @Nested
    @DisplayName("updateOrderStatus")
    class UpdateOrderStatus {

        @Test
        @DisplayName("should update order status to SHIPPED")
        void shouldUpdateStatus() {
            Order order = Order.builder()
                    .id(1L).user(sampleUser).totalAmount(new BigDecimal("134900.00"))
                    .status(Order.OrderStatus.PENDING).shippingAddress("Bengaluru")
                    .createdAt(LocalDateTime.now()).items(List.of()).build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            OrderResponse response = orderService.updateOrderStatus(1L, "SHIPPED");

            assertThat(response.getStatus()).isEqualTo("SHIPPED");
        }
    }

    @Nested
    @DisplayName("getOrderAnalytics")
    class GetOrderAnalytics {

        @Test
        @DisplayName("should return correct analytics using Streams")
        void shouldReturnAnalytics() {
            Order order1 = Order.builder().id(1L).user(sampleUser)
                    .totalAmount(new BigDecimal("1000.00")).status(Order.OrderStatus.DELIVERED)
                    .items(List.of()).createdAt(LocalDateTime.now()).build();
            Order order2 = Order.builder().id(2L).user(sampleUser)
                    .totalAmount(new BigDecimal("2000.00")).status(Order.OrderStatus.DELIVERED)
                    .items(List.of()).createdAt(LocalDateTime.now()).build();
            Order order3 = Order.builder().id(3L).user(sampleUser)
                    .totalAmount(new BigDecimal("3000.00")).status(Order.OrderStatus.PENDING)
                    .items(List.of()).createdAt(LocalDateTime.now()).build();

            when(orderRepository.findByUserIdOrderByCreatedAtDesc(any(), any()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(order1, order2, order3)));

            var analytics = orderService.getOrderAnalytics(1L);

            assertThat(analytics.get("totalOrders")).isEqualTo(3);
            assertThat(analytics.get("totalSpent")).isEqualTo(new BigDecimal("6000.00"));
            assertThat((double) analytics.get("averageOrderValue")).isEqualTo(2000.0);

            @SuppressWarnings("unchecked")
            var statusMap = (java.util.Map<String, Long>) analytics.get("ordersByStatus");
            assertThat(statusMap.get("DELIVERED")).isEqualTo(2L);
            assertThat(statusMap.get("PENDING")).isEqualTo(1L);
        }
    }
}
