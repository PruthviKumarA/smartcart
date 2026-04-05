# SmartCart - AI-Powered E-Commerce Platform

A microservices-based e-commerce platform with AI-powered product recommendations, built with Java 17, Spring Boot 3.2, and modern cloud-native technologies.

## Architecture

```
┌─────────────────┐     Kafka      ┌──────────────────────┐
│  Order Service   │ ──────────── │ Recommendation Service │
│  (Port 8081)     │   Events     │  (Port 8082)           │
│                  │              │                        │
│ • Products CRUD  │              │ • Kafka Consumer       │
│ • Cart & Orders  │              │ • Claude AI API        │
│ • Wishlist       │              │ • Redis Cache          │
│ • JWT Auth/RBAC  │              │ • Personalized Recs    │
│ • Async Processing│             │                        │
└────────┬─────────┘              └───────────┬────────────┘
         │                                     │
    PostgreSQL                              Redis
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.4 |
| Security | Spring Security, JWT (jjwt), BCrypt, RBAC |
| Database | PostgreSQL (prod), H2 (dev) |
| ORM | Spring Data JPA / Hibernate 6 |
| Caching | Redis |
| Messaging | Apache Kafka |
| AI | Anthropic Claude API |
| Containerization | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, MockMvc |
| Build | Maven (multi-module) |

## Key Features

### Order Service
- **Product Management**: Full CRUD with pagination, search, filtering by category/price range
- **JWT Authentication**: Stateless auth with role-based access control (CUSTOMER, ADMIN)
- **Order Processing**: Async order processing using `CompletableFuture` + custom `ThreadPoolTaskExecutor`
- **Wishlist**: Add/remove products, feeds into recommendation engine
- **Java Streams**: Used for order analytics (groupingBy, averaging, reducing)
- **Validation**: Bean validation with custom error handling
- **Soft Deletes**: Products are deactivated, not deleted

### Recommendation Service
- **Kafka Consumer**: Listens to wishlist and order events
- **AI Recommendations**: Calls Claude API with user's wishlist/purchase history
- **Redis Caching**: Caches recommendations with TTL-based expiry
- **Scheduled Refresh**: Background thread pool refreshes recommendations periodically

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for Redis, Kafka, PostgreSQL)

### Run Locally (H2 Database, no Docker needed)

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/smartcart.git
cd smartcart

# Build
mvn clean install

# Run Order Service
cd order-service
mvn spring-boot:run
```

The Order Service will start on `http://localhost:8081` with H2 in-memory database.

### API Endpoints

#### Auth
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login, get JWT token | No |

#### Products
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/products?page=0&size=10` | List products (paginated) | No |
| GET | `/api/products/{id}` | Get product by ID | No |
| GET | `/api/products/search?keyword=phone` | Search products | No |
| GET | `/api/products/category/{category}` | Filter by category | No |
| GET | `/api/products/price-range?min=10&max=100` | Filter by price | No |
| POST | `/api/products` | Create product | ADMIN |
| PUT | `/api/products/{id}` | Update product | ADMIN |
| DELETE | `/api/products/{id}` | Delete product (soft) | ADMIN |

#### Orders
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/orders` | Create order | Yes |
| GET | `/api/orders` | Get my orders | Yes |
| GET | `/api/orders/{id}` | Get order by ID | Yes |
| PATCH | `/api/orders/{id}/status?status=SHIPPED` | Update status | ADMIN |
| GET | `/api/orders/analytics` | Order analytics | Yes |

#### Wishlist
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/wishlist/{productId}` | Add to wishlist | Yes |
| GET | `/api/wishlist` | Get wishlist | Yes |
| DELETE | `/api/wishlist/{productId}` | Remove from wishlist | Yes |

### Sample API Calls

```bash
# Register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Pruthvi","email":"pruthvi@test.com","password":"password123"}'

# Login (save the token!)
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"pruthvi@test.com","password":"password123"}'

# Create Product (Admin)
curl -X POST http://localhost:8081/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 15","description":"Latest Apple phone","price":79999,"category":"Electronics","stockQuantity":50}'

# Search Products
curl "http://localhost:8081/api/products/search?keyword=phone&page=0&size=5"

# Create Order
curl -X POST http://localhost:8081/api/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress":"Bengaluru, KA","items":[{"productId":1,"quantity":2}]}'
```

## Project Structure

```
smartcart/
├── pom.xml                          # Parent POM (multi-module)
├── order-service/
│   ├── pom.xml
│   └── src/main/java/com/smartcart/order/
│       ├── OrderServiceApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java          # Spring Security + JWT filter chain
│       │   └── AsyncConfig.java             # Thread pool configuration
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── ProductController.java
│       │   ├── OrderController.java
│       │   └── WishlistController.java
│       ├── dto/
│       │   ├── AuthDtos.java
│       │   ├── ProductDtos.java
│       │   └── OrderDtos.java
│       ├── entity/
│       │   ├── User.java                    # With Role enum for RBAC
│       │   ├── Product.java                 # With DB indexes
│       │   ├── Order.java                   # With OrderStatus enum
│       │   ├── OrderItem.java
│       │   └── WishlistItem.java
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│       │   ├── ResourceNotFoundException.java
│       │   ├── BadRequestException.java
│       │   └── DuplicateResourceException.java
│       ├── repository/
│       │   ├── UserRepository.java
│       │   ├── ProductRepository.java       # Custom queries, pagination
│       │   ├── OrderRepository.java
│       │   └── WishlistRepository.java
│       ├── security/
│       │   ├── JwtUtil.java                 # Token generation/validation
│       │   ├── JwtAuthenticationFilter.java # OncePerRequestFilter
│       │   └── UserPrincipal.java           # UserDetails implementation
│       └── service/
│           ├── AuthService.java
│           ├── ProductService.java
│           ├── OrderService.java            # Async + Streams + CompletableFuture
│           └── WishlistService.java
└── recommendation-service/                  # Day 5
    └── ...
```

## Author

**Pruthvi Kumar A** - Software Engineer
- LinkedIn: [pruthvi-kumar-2000](https://linkedin.com/in/pruthvi-kumar-2000)
- Email: pruthvikumar99.pkpk@gmail.com
