# Mini iFood API

Portfolio-grade REST API for food ordering, built with Spring Boot, JWT authentication, PostgreSQL, Redis caching, and automated tests.

## Overview

This project simulates a real production backend with:

- Layered architecture by feature (`auth`, `user`, `product`, `order`, `payment`)
- Stateless authentication and authorization using Spring Security + JWT
- Order lifecycle with status transition rules
- Caching strategy for product listing with Redis
- Automated test suite (unit + integration + security scenarios)

## Tech Stack

- Java 21
- Spring Boot 4.0.4
- Spring Security (JWT)
- Spring Data JPA + Hibernate
- Flyway
- PostgreSQL
- Redis
- OpenAPI / Swagger
- Maven
- GitHub Actions

## Project Structure

```text
src/main/java/com/marcosdias/miniifood/
  auth/       Authentication (login/register)
  user/       User management
  product/    Product CRUD + cache integration
  order/      Order aggregate and status flow
  payment/    Mock payment flow
  security/   JWT service, filter, security configuration
  config/     Infrastructure configuration (cache)
```

## Main Features

- User registration and login
- JWT token issuance and request authentication
- Product CRUD with pagination
- Order creation, listing, status updates, and cancellation rules
- Role-based access on admin order operations
- Mock payment processing integrated with order status
- Redis cache on product listing
- Database versioning with Flyway migrations

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Local Setup

Requirements:

- JDK 21+
- Maven 3.9+
- Docker + Docker Compose

Set environment variables (PowerShell):

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="5433"
$env:DB_NAME="mini_ifood"
$env:DB_USER="postgres"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your-secret-key-min-32-chars"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
```

Start infrastructure (PostgreSQL + Redis):

```powershell
docker compose up -d
docker compose ps
```

Run tests and application:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Stop infrastructure:

```powershell
docker compose down
```

## Postman Collection

- Collection file: `MiniIfoodAPI.postman_collection.json`
- Import this file in Postman to test auth and product cache flow
- Run `Auth -> Login User` first to populate `auth_token`

If cache data gets incompatible after serializer changes:

```powershell
docker exec -it mini-ifood-cache redis-cli FLUSHALL
```

## Test Strategy

Current suite includes:

- Unit tests for service/business rules
- Integration tests for service and repository behavior
- Security-focused tests (JWT, filter, method authorization)
- HTTP-level integration tests for `OrderController` using `MockMvc`

Useful commands:

```powershell
.\mvnw.cmd test
.\mvnw.cmd test -Dtest=OrderControllerIntegrationTest
```

### Optional Advanced: Testcontainers

An optional Testcontainers baseline is included for PostgreSQL + Redis smoke testing.
It only runs when `RUN_TESTCONTAINERS=true`.

```powershell
$env:RUN_TESTCONTAINERS="true"
.\mvnw.cmd test -Dtest=ContainersSmokeTest
```

## Profiles

- `dev`: PostgreSQL + Redis
- `test`: H2 in-memory (fast CI tests)
- `prod`: PostgreSQL strict runtime config
- `tc`: Testcontainers support profile

## CI

GitHub Actions workflow runs tests on pushes and pull requests for `main`, `develop`, and `feature/**` branches.

## License

This project is licensed under the MIT License. See `LICENSE`.

