package com.smartcart.order.controller;

import com.smartcart.order.dto.ProductDtos.*;
import com.smartcart.order.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController @RequestMapping("/api/products") @RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) { return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(req)); }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(productService.getAllProducts(page, size, sortBy, direction));
    }

    @GetMapping("/{id}") public ResponseEntity<ProductResponse> getById(@PathVariable Long id) { return ResponseEntity.ok(productService.getProductById(id)); }
    @GetMapping("/category/{category}") public ResponseEntity<Page<ProductResponse>> byCategory(@PathVariable String category, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) { return ResponseEntity.ok(productService.getByCategory(category, page, size)); }
    @GetMapping("/search") public ResponseEntity<Page<ProductResponse>> search(@RequestParam String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) { return ResponseEntity.ok(productService.searchProducts(keyword, page, size)); }
    @GetMapping("/price-range") public ResponseEntity<Page<ProductResponse>> byPrice(@RequestParam BigDecimal min, @RequestParam BigDecimal max, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) { return ResponseEntity.ok(productService.getByPriceRange(min, max, page, size)); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody CreateProductRequest req) { return ResponseEntity.ok(productService.updateProduct(id, req)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> delete(@PathVariable Long id) { productService.deleteProduct(id); return ResponseEntity.noContent().build(); }
}
