# SmartCart — AI-Powered E-Commerce Platform

A microservices-based e-commerce platform with **AI-powered product recommendations**, built with Java 17, Spring Boot 3.2, Kafka, Redis, and Claude AI.

## Architecture

```
┌──────────────────┐      Kafka       ┌─────────────────────┐
│  Order Service   │ ───────────────> │ Recommendation Svc  │
│  (Port 8081)     │  orders/wishlist │ (Port 8082)         │
│                  │                  │                     │
│ • Products CRUD  │                  │ • Kafka Consumer    │
│ • Cart & Orders  │                  │ • Claude AI API     │
│ • JWT Auth/RBAC  │                  │ • Redis Cache       │
│ • Redis Caching  │                  │ • Scheduled Refresh │
│ • Async Process  │                  │ • Fallback Strategy │
└────────┬─────────┘                  └──────────┬──────────┘
         │                                       │
    PostgreSQL/H2                             Redis
```

## Tech Stack

Java 17 | Spring Boot 3.2 | Spring Security + JWT | PostgreSQL/H2 | Redis | Apache Kafka | Claude AI | Docker | JUnit 5 + Mockito

## Quick Start

```bash
cd order-service
mvn clean install
mvn spring-boot:run
# Starts on http://localhost:8081 with H2 database
```

**Login:** `admin@smartcart.com / admin123` or `pruthvi@smartcart.com / password123`

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | No | Register |
| POST | `/api/auth/login` | No | Login (returns JWT) |
| GET | `/api/products` | No | List products (paginated, cached) |
| GET | `/api/products/search?keyword=iPhone` | No | Search |
| POST | `/api/products` | ADMIN | Create product |
| POST | `/api/orders` | USER | Place order (fires Kafka event) |
| GET | `/api/orders/analytics` | USER | Order analytics (Streams) |
| POST | `/api/wishlist/{id}` | USER | Add to wishlist (fires Kafka event) |
| GET | `/api/recommendations/user/{id}` | No | AI recommendations (port 8082) |

## With Docker

```bash
docker-compose -f docker-compose-infra.yml up -d   # Start PostgreSQL, Redis, Kafka
mvn spring-boot:run -Dspring-boot.run.profiles=docker  # Run with real infra
```

## Author

**Pruthvi Kumar A** — Software Engineer | 3+ Years | Java • Spring Boot • Microservices
