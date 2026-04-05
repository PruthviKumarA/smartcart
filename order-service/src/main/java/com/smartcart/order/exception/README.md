# 🛒 SmartCart — AI-Powered E-Commerce Platform

A microservices-based e-commerce platform with **AI-powered product recommendations**, built with Java 17, Spring Boot 3.2, Kafka, Redis, and Claude AI.

> **Built by [Pruthvi Kumar A](https://linkedin.com/in/pruthvi-kumar-2000)** — Software Engineer | Java • Spring Boot • Microservices

---

## 🏗️ Architecture

```
┌──────────────────────┐         Kafka          ┌──────────────────────────┐
│    Order Service     │ ─────────────────────▶ │  Recommendation Service  │
│    (Port 8081)       │    smartcart.orders     │  (Port 8082)             │
│                      │    smartcart.wishlist   │                          │
│  • Product CRUD      │                        │  • Kafka Consumer        │
│  • Cart & Orders     │                        │  • Claude AI Integration │
│  • Wishlist          │                        │  • Redis Cache (60m TTL) │
│  • JWT Auth + RBAC   │                        │  • Scheduled Refresh     │
│  • Async Processing  │                        │  • Fallback Suggestions  │
│  • Redis Caching     │                        │                          │
└──────────┬───────────┘                        └────────────┬─────────────┘
           │                                                  │
      PostgreSQL                                           Redis
      (Persistent)                                        (Cache)
```

## 🔧 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.4 (jakarta namespace) |
| **Security** | Spring Security, JWT (jjwt 0.12.5), BCrypt, RBAC |
| **Database** | PostgreSQL 16 (prod), H2 (dev) |
| **ORM** | Spring Data JPA / Hibernate 6 |
| **Caching** | Redis 7 with TTL-based expiry |
| **Messaging** | Apache Kafka (3 partitions, JSON serialization) |
| **AI** | Anthropic Claude API (claude-sonnet) |
| **Async** | CompletableFuture + ThreadPoolTaskExecutor |
| **Containers** | Docker, Docker Compose |
| **Testing** | JUnit 5, Mockito, AssertJ |
| **Build** | Maven |

## ✨ Key Features

### Order Service
- **JWT Authentication** with role-based access control (CUSTOMER, ADMIN)
- **Product Management** — Full CRUD with pagination, search, category filter, price range filter
- **Redis Caching** — `@Cacheable` on reads, `@CacheEvict` on writes, TTL 10-30 minutes
- **Order Processing** — Stock validation, N+1 query prevention, async post-processing
- **Async with CompletableFuture** — Order notifications and analytics run on background threads via custom `ThreadPoolTaskExecutor`
- **Kafka Producer** — Publishes ORDER_CREATED and WISHLIST events
- **Java Streams** — Order analytics (groupingBy, averaging, reducing)
- **Data Seeder** — Auto-populates 18 products across 5 categories on startup
- **Global Exception Handler** — `@RestControllerAdvice` with custom exceptions

### Recommendation Service
- **Kafka Consumer** — Listens to order and wishlist events from Order Service
- **Claude AI Integration** — Generates personalized product suggestions based on user behavior
- **Redis Caching** — Recommendations cached with 60-min TTL, invalidated on user activity
- **Scheduled Refresh** — `@Scheduled` cron job refreshes recommendations every 6 hours
- **Fallback Strategy** — Category-based suggestions when AI API is unavailable
- **Thread-Safe Tracking** — `ConcurrentHashMap` for multi-threaded Kafka consumer access
- **Error Handling** — Kafka retry with `FixedBackOff`, dead letter logging

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker Desktop (for PostgreSQL, Redis, Kafka)

### Option A: Run Locally (H2, no Docker needed)

```bash
cd order-service
mvn clean install
mvn spring-boot:run
# App starts on http://localhost:8081 with H2 database
# Kafka events silently skipped, Redis falls back to in-memory cache
```

### Option B: Full Stack with Docker

```bash
# Start infrastructure
docker-compose -f docker-compose-infra.yml up -d

# Run Order Service (with Docker profile)
cd order-service
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Run Recommendation Service (in another terminal)
cd recommendation-service
mvn spring-boot:run
```

### Pre-seeded Test Accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@smartcart.com | admin123 |
| Customer | pruthvi@smartcart.com | password123 |

## 📡 API Endpoints

### Auth
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | No | Register new user |
| POST | `/api/auth/login` | No | Login, returns JWT |

### Products
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/products?page=0&size=10&sortBy=price&direction=asc` | No | Paginated list |
| GET | `/api/products/{id}` | No | Get by ID (cached) |
| GET | `/api/products/search?keyword=iPhone` | No | Search |
| GET | `/api/products/category/Electronics` | No | Filter by category |
| GET | `/api/products/price-range?min=500&max=5000` | No | Filter by price |
| POST | `/api/products` | ADMIN | Create product |
| PUT | `/api/products/{id}` | ADMIN | Update product |
| DELETE | `/api/products/{id}` | ADMIN | Soft delete |

### Orders
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/orders` | USER | Create order (fires Kafka event) |
| GET | `/api/orders` | USER | My orders (paginated) |
| GET | `/api/orders/{id}` | USER | Order details |
| GET | `/api/orders/analytics` | USER | Order analytics (Streams) |
| PATCH | `/api/orders/{id}/status?status=SHIPPED` | ADMIN | Update status |

### Wishlist
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/wishlist/{productId}` | USER | Add (fires Kafka event) |
| GET | `/api/wishlist` | USER | Get wishlist |
| DELETE | `/api/wishlist/{productId}` | USER | Remove |

### Recommendations (Port 8082)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/recommendations/user/{userId}` | AI recommendations |
| GET | `/api/recommendations/activity/{userId}` | User's tracked activity |
| POST | `/api/recommendations/refresh/{userId}` | Force refresh |
| GET | `/api/recommendations/health` | Service health |

## 🧪 Testing

```bash
# Run all tests
cd order-service && mvn test

# Test results:
# ProductServiceTest  — 7 tests (CRUD, search, pagination)
# OrderServiceTest    — 7 tests (create, stock validation, analytics)
# AuthServiceTest     — 4 tests (register, login, duplicates)
```

## 📂 Project Structure

```
smartcart/
├── docker-compose.yml
├── docker-compose-infra.yml
├── order-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/smartcart/order/
│       ├── config/
│       │   ├── SecurityConfig.java         # Spring Security 3.x + JWT
│       │   ├── AsyncConfig.java            # ThreadPoolTaskExecutor
│       │   ├── KafkaConfig.java            # Producer + topics
│       │   ├── RedisConfig.java            # Cache with TTL
│       │   └── DataSeeder.java             # 18 products + 3 users
│       ├── controller/                     # REST endpoints
│       ├── dto/                            # Request/Response DTOs
│       ├── entity/                         # JPA entities with indexes
│       ├── event/                          # Kafka events + publisher
│       ├── exception/                      # Global error handling
│       ├── repository/                     # Spring Data JPA
│       ├── security/                       # JWT filter + UserPrincipal
│       └── service/                        # Business logic
└── recommendation-service/
    ├── pom.xml
    └── src/main/java/com/smartcart/recommendation/
        ├── client/ClaudeApiClient.java     # AI API integration
        ├── config/                         # Kafka consumer + Redis
        ├── consumer/EventConsumer.java     # Kafka listeners
        ├── controller/                     # REST endpoints
        ├── dto/                            # Event + response models
        └── service/
            ├── RecommendationService.java  # AI + cache orchestration
            └── UserActivityTracker.java    # Thread-safe activity store
```

## 💡 Design Decisions & Interview Talking Points

| Decision | Why |
|----------|-----|
| **Kafka for inter-service communication** | Decouples services — if Recommendation Service is down, orders still work. Events persist until consumed. |
| **Redis caching with TTL** | Products are read-heavy, write-light. 30-min TTL for individual products, 10-min for listings. Cache eviction on writes. |
| **CompletableFuture for async** | Order confirmation returns immediately while email/analytics processing happens in background on thread pool. |
| **ConcurrentHashMap in UserActivityTracker** | Kafka consumer threads and REST API threads both access user data. ConcurrentHashMap provides thread-safe access without global locks. |
| **JWT + RBAC** | Stateless authentication — no server-side session. Roles encoded in token claims. Filter chain validates on every request. |
| **Spring Boot 3.x** | jakarta namespace, SecurityFilterChain bean pattern, Java 17+ features. |
| **Fallback recommendations** | If Claude API is down, returns category-based suggestions instead of failing. Graceful degradation. |
| **Multi-stage Docker build** | Build stage compiles JAR, run stage uses slim JRE. Final image ~200MB vs ~800MB. |
| **H2 + PostgreSQL profiles** | Default profile uses H2 for zero-setup local dev. Docker profile switches to real PostgreSQL. |

## 👤 Author

**Pruthvi Kumar A** — Software Engineer | 3+ Years Experience

- 🔗 [LinkedIn](https://linkedin.com/in/pruthvi-kumar-2000)
- 📧 pruthvikumar99.pkpk@gmail.com
- 📍 Bengaluru, India
