package com.smartcart.order.repository;

import com.smartcart.order.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByCategoryAndActiveTrue(String category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.price BETWEEN :min AND :max")
    Page<Product> findByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND LOWER(p.name) LIKE LOWER(CONCAT(\'%\', :kw, \'%\'))")
    Page<Product> searchByKeyword(@Param("kw") String keyword, Pageable pageable);

    List<Product> findByIdIn(List<Long> ids);
}
