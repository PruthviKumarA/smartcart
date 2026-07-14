package com.smartcart.order.service;

import com.smartcart.order.dto.ProductDtos.*;
import com.smartcart.order.entity.Product;
import com.smartcart.order.exception.ResourceNotFoundException;
import com.smartcart.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service @RequiredArgsConstructor @Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional @CacheEvict(value = "productListings", allEntries = true)
    public ProductResponse createProduct(CreateProductRequest req) {
        Product p = Product.builder().name(req.getName()).description(req.getDescription()).price(req.getPrice())
                .category(req.getCategory()).stockQuantity(req.getStockQuantity()).imageUrl(req.getImageUrl()).active(true).build();
        return mapToResponse(productRepository.save(p));
    }

    @Transactional(readOnly = true) @Cacheable(value = "productListings", key = "#page + '-' + #size + '-' + #sortBy + '-' + #dir")
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String dir) {
        log.info("Cache MISS - fetching from DB");
        Sort sort = dir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return productRepository.findByActiveTrue(PageRequest.of(page, size, sort)).map(this::mapToResponse);
    }

    @Transactional(readOnly = true) @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        log.info("Cache MISS - fetching product {}", id);
        return mapToResponse(productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id)));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getByCategory(String cat, int page, int size) {
        return productRepository.findByCategoryAndActiveTrue(cat, PageRequest.of(page, size, Sort.by("price"))).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String kw, int page, int size) {
        return productRepository.searchByKeyword(kw, PageRequest.of(page, size)).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getByPriceRange(BigDecimal min, BigDecimal max, int page, int size) {
        return productRepository.findByPriceRange(min, max, PageRequest.of(page, size, Sort.by("price"))).map(this::mapToResponse);
    }

    @Transactional @CacheEvict(value = {"products", "productListings"}, allEntries = true)
    public ProductResponse updateProduct(Long id, CreateProductRequest req) {
        Product p = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        p.setName(req.getName()); p.setDescription(req.getDescription()); p.setPrice(req.getPrice());
        p.setCategory(req.getCategory()); p.setStockQuantity(req.getStockQuantity()); p.setImageUrl(req.getImageUrl());
        return mapToResponse(productRepository.save(p));
    }

    @Transactional @CacheEvict(value = {"products", "productListings"}, allEntries = true)
    public void deleteProduct(Long id) {
        Product p = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        p.setActive(false); productRepository.save(p);
    }

    private ProductResponse mapToResponse(Product p) {
        return ProductResponse.builder().id(p.getId()).name(p.getName()).description(p.getDescription()).price(p.getPrice())
                .category(p.getCategory()).stockQuantity(p.getStockQuantity()).imageUrl(p.getImageUrl()).active(p.getActive()).createdAt(p.getCreatedAt()).build();
    }
}
