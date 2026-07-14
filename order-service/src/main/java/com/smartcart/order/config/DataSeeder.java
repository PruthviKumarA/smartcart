package com.smartcart.order.config;

import com.smartcart.order.entity.*;
import com.smartcart.order.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component @RequiredArgsConstructor @Slf4j
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) return;
        log.info("Seeding database...");

        userRepo.save(User.builder().name("Admin User").email("admin@smartcart.com").password(encoder.encode("admin123")).role(User.Role.ADMIN).build());
        userRepo.save(User.builder().name("Pruthvi Kumar").email("pruthvi@smartcart.com").password(encoder.encode("password123")).role(User.Role.CUSTOMER).build());
        userRepo.save(User.builder().name("Test Customer").email("test@smartcart.com").password(encoder.encode("password123")).role(User.Role.CUSTOMER).build());

        productRepo.saveAll(List.of(
            Product.builder().name("iPhone 15 Pro").description("Apple smartphone with A17 Pro chip").price(new BigDecimal("134900.00")).category("Electronics").stockQuantity(50).active(true).build(),
            Product.builder().name("Samsung Galaxy S24 Ultra").description("Samsung flagship with S Pen").price(new BigDecimal("129999.00")).category("Electronics").stockQuantity(35).active(true).build(),
            Product.builder().name("MacBook Air M3").description("Apple laptop with M3 chip").price(new BigDecimal("139900.00")).category("Electronics").stockQuantity(25).active(true).build(),
            Product.builder().name("Sony WH-1000XM5").description("Noise-cancelling headphones").price(new BigDecimal("29990.00")).category("Electronics").stockQuantity(100).active(true).build(),
            Product.builder().name("iPad Air M2").description("Apple tablet with M2 chip").price(new BigDecimal("69900.00")).category("Electronics").stockQuantity(40).active(true).build(),
            Product.builder().name("Clean Code by Robert Martin").description("Agile software craftsmanship").price(new BigDecimal("499.00")).category("Books").stockQuantity(200).active(true).build(),
            Product.builder().name("Designing Data-Intensive Applications").description("By Martin Kleppmann").price(new BigDecimal("699.00")).category("Books").stockQuantity(150).active(true).build(),
            Product.builder().name("System Design Interview Vol 1").description("By Alex Xu").price(new BigDecimal("599.00")).category("Books").stockQuantity(180).active(true).build(),
            Product.builder().name("Nike Air Max 270").description("Running shoes").price(new BigDecimal("12995.00")).category("Clothing").stockQuantity(75).active(true).build(),
            Product.builder().name("Levi's 511 Slim Fit Jeans").description("Dark wash stretch denim").price(new BigDecimal("3499.00")).category("Clothing").stockQuantity(60).active(true).build(),
            Product.builder().name("Instant Pot Duo 7-in-1").description("Electric pressure cooker").price(new BigDecimal("8999.00")).category("Home").stockQuantity(45).active(true).build(),
            Product.builder().name("Dyson V15 Detect Vacuum").description("Cordless vacuum with laser").price(new BigDecimal("52900.00")).category("Home").stockQuantity(20).active(true).build(),
            Product.builder().name("Apple Watch Series 9").description("GPS + Cellular smartwatch").price(new BigDecimal("49900.00")).category("Fitness").stockQuantity(55).active(true).build(),
            Product.builder().name("Yoga Mat Premium 6mm").description("Non-slip exercise mat").price(new BigDecimal("1299.00")).category("Fitness").stockQuantity(150).active(true).build()
        ));

        log.info("=== DATABASE SEEDED: admin@smartcart.com/admin123, pruthvi@smartcart.com/password123 ===");
    }
}
