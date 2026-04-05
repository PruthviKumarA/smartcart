package com.smartcart.order.config;

import com.smartcart.order.entity.Product;
import com.smartcart.order.entity.User;
import com.smartcart.order.repository.ProductRepository;
import com.smartcart.order.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with sample data on startup.
 * Only runs if DB is empty (safe for repeated restarts).
 * 
 * This gives you a working API immediately — no manual setup needed.
 * Admin user: admin@smartcart.com / admin123
 * Customer user: pruthvi@smartcart.com / password123
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping");
            return;
        }

        log.info("Seeding database with sample data...");

        // === USERS ===
        User admin = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@smartcart.com")
                .password(passwordEncoder.encode("admin123"))
                .role(User.Role.ADMIN)
                .build());

        User customer = userRepository.save(User.builder()
                .name("Pruthvi Kumar")
                .email("pruthvi@smartcart.com")
                .password(passwordEncoder.encode("password123"))
                .role(User.Role.CUSTOMER)
                .build());

        User customer2 = userRepository.save(User.builder()
                .name("Test Customer")
                .email("test@smartcart.com")
                .password(passwordEncoder.encode("password123"))
                .role(User.Role.CUSTOMER)
                .build());

        log.info("Created {} users", userRepository.count());

        // === PRODUCTS ===
        List<Product> products = List.of(
                // Electronics
                Product.builder().name("iPhone 15 Pro").description("Latest Apple smartphone with A17 Pro chip, titanium design, 48MP camera").price(new BigDecimal("134900.00")).category("Electronics").stockQuantity(50).imageUrl("https://example.com/iphone15.jpg").active(true).build(),
                Product.builder().name("Samsung Galaxy S24 Ultra").description("Samsung flagship with S Pen, 200MP camera, Snapdragon 8 Gen 3").price(new BigDecimal("129999.00")).category("Electronics").stockQuantity(35).imageUrl("https://example.com/s24ultra.jpg").active(true).build(),
                Product.builder().name("MacBook Air M3").description("Apple laptop with M3 chip, 15-inch Liquid Retina display, 18hr battery").price(new BigDecimal("139900.00")).category("Electronics").stockQuantity(25).imageUrl("https://example.com/macbookair.jpg").active(true).build(),
                Product.builder().name("Sony WH-1000XM5").description("Premium noise-cancelling wireless headphones, 30hr battery life").price(new BigDecimal("29990.00")).category("Electronics").stockQuantity(100).imageUrl("https://example.com/sonywh.jpg").active(true).build(),
                Product.builder().name("iPad Air M2").description("Apple tablet with M2 chip, 11-inch Liquid Retina display").price(new BigDecimal("69900.00")).category("Electronics").stockQuantity(40).imageUrl("https://example.com/ipadair.jpg").active(true).build(),

                // Books
                Product.builder().name("Clean Code by Robert Martin").description("A handbook of agile software craftsmanship").price(new BigDecimal("499.00")).category("Books").stockQuantity(200).imageUrl("https://example.com/cleancode.jpg").active(true).build(),
                Product.builder().name("Designing Data-Intensive Applications").description("By Martin Kleppmann - The big ideas behind reliable, scalable systems").price(new BigDecimal("699.00")).category("Books").stockQuantity(150).imageUrl("https://example.com/ddia.jpg").active(true).build(),
                Product.builder().name("System Design Interview Vol 1").description("By Alex Xu - Step-by-step approach to system design").price(new BigDecimal("599.00")).category("Books").stockQuantity(180).imageUrl("https://example.com/sdi.jpg").active(true).build(),
                Product.builder().name("Java Concurrency in Practice").description("By Brian Goetz - Definitive guide to Java threading").price(new BigDecimal("549.00")).category("Books").stockQuantity(120).imageUrl("https://example.com/jcip.jpg").active(true).build(),

                // Clothing
                Product.builder().name("Nike Air Max 270").description("Men's running shoes with Max Air unit, lightweight mesh upper").price(new BigDecimal("12995.00")).category("Clothing").stockQuantity(75).imageUrl("https://example.com/nikeair.jpg").active(true).build(),
                Product.builder().name("Levi's 511 Slim Fit Jeans").description("Classic slim fit jeans in dark wash, stretch denim").price(new BigDecimal("3499.00")).category("Clothing").stockQuantity(60).imageUrl("https://example.com/levis511.jpg").active(true).build(),
                Product.builder().name("Uniqlo Dry-EX Crew Neck T-Shirt").description("Quick-drying performance t-shirt for active wear").price(new BigDecimal("990.00")).category("Clothing").stockQuantity(200).imageUrl("https://example.com/uniqlo.jpg").active(true).build(),

                // Home & Kitchen
                Product.builder().name("Instant Pot Duo 7-in-1").description("Electric pressure cooker, slow cooker, rice cooker, steamer, saute, yogurt maker, warmer").price(new BigDecimal("8999.00")).category("Home").stockQuantity(45).imageUrl("https://example.com/instantpot.jpg").active(true).build(),
                Product.builder().name("Dyson V15 Detect Vacuum").description("Cordless vacuum with laser dust detection, LCD screen").price(new BigDecimal("52900.00")).category("Home").stockQuantity(20).imageUrl("https://example.com/dysonv15.jpg").active(true).build(),
                Product.builder().name("Philips Air Fryer XXL").description("Twin TurboStar technology, 1.4kg capacity, digital display").price(new BigDecimal("14999.00")).category("Home").stockQuantity(30).imageUrl("https://example.com/airfryer.jpg").active(true).build(),

                // Fitness
                Product.builder().name("Apple Watch Series 9").description("GPS + Cellular, 45mm, health monitoring, workout tracking").price(new BigDecimal("49900.00")).category("Fitness").stockQuantity(55).imageUrl("https://example.com/applewatch.jpg").active(true).build(),
                Product.builder().name("Yoga Mat Premium 6mm").description("Non-slip exercise mat with carrying strap, eco-friendly TPE").price(new BigDecimal("1299.00")).category("Fitness").stockQuantity(150).imageUrl("https://example.com/yogamat.jpg").active(true).build(),
                Product.builder().name("Resistance Bands Set (5 Pack)").description("Latex exercise bands with different resistance levels").price(new BigDecimal("799.00")).category("Fitness").stockQuantity(200).imageUrl("https://example.com/bands.jpg").active(true).build()
        );

        productRepository.saveAll(products);
        log.info("Created {} products across {} categories", products.size(),
                products.stream().map(Product::getCategory).distinct().count());

        log.info("=== DATABASE SEEDED SUCCESSFULLY ===");
        log.info("Admin login: admin@smartcart.com / admin123");
        log.info("Customer login: pruthvi@smartcart.com / password123");
        log.info("Products: {} items in categories: Electronics, Books, Clothing, Home, Fitness",
                products.size());
    }
}
