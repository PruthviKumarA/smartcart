package com.smartcart.order.service;

import com.smartcart.order.dto.ProductDtos.*;
import com.smartcart.order.entity.Product;
import com.smartcart.order.exception.ResourceNotFoundException;
import com.smartcart.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 * Uses Mockito to mock the repository layer.
 * 
 * INTERVIEW: "How do you test your services?"
 * "I use JUnit 5 with Mockito. I mock the repository layer so tests are fast
 * and don't need a database. I test happy paths, edge cases, and error scenarios.
 * I use @DisplayName for readable test names and @Nested for grouping related tests."
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("iPhone 15")
                .description("Latest Apple phone")
                .price(new BigDecimal("134900.00"))
                .category("Electronics")
                .stockQuantity(50)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = new CreateProductRequest();
        createRequest.setName("iPhone 15");
        createRequest.setDescription("Latest Apple phone");
        createRequest.setPrice(new BigDecimal("134900.00"));
        createRequest.setCategory("Electronics");
        createRequest.setStockQuantity(50);
        createRequest.setImageUrl("https://example.com/iphone.jpg");
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("should create product and return response")
        void shouldCreateProduct() {
            // Arrange
            when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

            // Act
            ProductResponse response = productService.createProduct(createRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("iPhone 15");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("134900.00"));
            assertThat(response.getCategory()).isEqualTo("Electronics");
            assertThat(response.getActive()).isTrue();

            verify(productRepository, times(1)).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("should return product when found")
        void shouldReturnProduct_whenFound() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            // Act
            ProductResponse response = productService.getProductById(1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("iPhone 15");
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrow_whenNotFound() {
            // Arrange
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found with id: 999");
        }
    }

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("should return paginated products sorted by price ascending")
        void shouldReturnPaginatedProducts() {
            // Arrange
            Product product2 = Product.builder()
                    .id(2L).name("Samsung Galaxy").price(new BigDecimal("129999.00"))
                    .category("Electronics").stockQuantity(30).active(true)
                    .createdAt(LocalDateTime.now()).build();

            Page<Product> page = new PageImpl<>(List.of(sampleProduct, product2),
                    PageRequest.of(0, 10, Sort.by("price").ascending()), 2);

            when(productRepository.findByActiveTrue(any(Pageable.class))).thenReturn(page);

            // Act
            Page<ProductResponse> result = productService.getAllProducts(0, 10, "price", "asc");

            // Assert
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).getName()).isEqualTo("iPhone 15");
        }

        @Test
        @DisplayName("should return empty page when no products exist")
        void shouldReturnEmptyPage() {
            // Arrange
            Page<Product> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(productRepository.findByActiveTrue(any(Pageable.class))).thenReturn(emptyPage);

            // Act
            Page<ProductResponse> result = productService.getAllProducts(0, 10, "id", "asc");

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("should update product fields and return updated response")
        void shouldUpdateProduct() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            CreateProductRequest updateRequest = new CreateProductRequest();
            updateRequest.setName("iPhone 15 Pro Max");
            updateRequest.setDescription("Updated description");
            updateRequest.setPrice(new BigDecimal("179900.00"));
            updateRequest.setCategory("Electronics");
            updateRequest.setStockQuantity(25);
            updateRequest.setImageUrl("https://example.com/iphone15promax.jpg");

            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ProductResponse response = productService.updateProduct(1L, updateRequest);

            // Assert
            assertThat(response.getName()).isEqualTo("iPhone 15 Pro Max");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("179900.00"));
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should throw when updating non-existent product")
        void shouldThrow_whenUpdatingNonExistent() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(999L, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("should soft-delete product by setting active to false")
        void shouldSoftDeleteProduct() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            productService.deleteProduct(1L);

            // Assert
            assertThat(sampleProduct.getActive()).isFalse();  // soft deleted
            verify(productRepository).save(sampleProduct);
        }
    }

    @Nested
    @DisplayName("searchProducts")
    class SearchProducts {

        @Test
        @DisplayName("should return products matching keyword")
        void shouldReturnMatchingProducts() {
            // Arrange
            Page<Product> page = new PageImpl<>(List.of(sampleProduct));
            when(productRepository.searchByKeyword(eq("iPhone"), any(Pageable.class))).thenReturn(page);

            // Act
            Page<ProductResponse> result = productService.searchProducts("iPhone", 0, 10);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).contains("iPhone");
        }
    }
}
