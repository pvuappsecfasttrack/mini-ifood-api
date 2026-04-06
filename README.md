# Mini iFood API

Portfolio-grade REST API for food ordering, built with Spring Boot, JWT authentication, PostgreSQL, Redis caching, and automated tests.

## Overview

This project simulates a real production backend with:

- Layered architecture by feature (`auth`, `user`, `product`, `order`, `payment`)
- Stateless authentication and authorization using Spring Security + JWT
- Order lifecycle with status transition rules
- Caching strategy for product listing with Redis
- Automated test suite (unit + integration + security scenarios)

## Why This Project

This API was designed to demonstrate practical backend skills expected in production projects:

- Clean domain separation and maintainable package structure
- Security-first approach with JWT-based stateless authentication
- Reliable persistence with PostgreSQL and Flyway migrations
- Performance optimization with Redis caching
- Test strategy across unit, integration, and HTTP/security layers
- Operational readiness through metrics and dashboards

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

## Quick Start

Run everything with Docker (app + PostgreSQL + Redis + Prometheus + Grafana):

```powershell
docker compose up -d --build
docker compose ps
```

Create a local environment file once (recommended):

```powershell
Copy-Item .env.example .env
```

Optional environment overrides (PowerShell):

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

If you prefer running only infrastructure with Docker and app on host:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Stop infrastructure:

```powershell
docker compose down
```

If the cache schema changes between runs, clear Redis once:

```powershell
docker exec -it mini-ifood-cache redis-cli FLUSHALL
```

## Postman Collection

- Collection file: `MiniIfoodAPI.postman_collection.json`
- Import this file in Postman to test auth and product cache flow
- Run `Auth -> Login User` first to populate `auth_token`
- Then run `Products -> Get All Products (CACHE TEST 1/2)` to see Redis cache behavior

## Test Strategy

Current suite includes:

- Unit tests for service/business rules
- Integration tests for service and repository behavior
- Security-focused tests (JWT, filter, method authorization)
- HTTP-level tests for `AuthController` and `ProductController` using `@WebMvcTest` + `MockMvc`
- HTTP-level integration tests for `OrderController` using `MockMvc`

Useful commands:

```powershell
.\mvnw.cmd test
.\mvnw.cmd test -Dtest=OrderControllerIntegrationTest
.\mvnw.cmd test "-Dtest=AuthControllerMvcTest,ProductControllerMvcTest"
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

## Monitoring (Prometheus + Grafana)

This project already exposes metrics through `http://localhost:8080/actuator/prometheus`.

- `Prometheus` collects and stores metrics over time (it does not render dashboards).
- `Grafana` reads data from Prometheus and renders charts/dashboards.

Most teams run both with Docker because setup is faster and reproducible.
In this project they are already part of `docker-compose.yml`.

Open:

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (default login `admin` / `admin`)

Prometheus targets expected as `UP`:

- `app:8080`
- `postgres-exporter:9187`
- `redis-exporter:9121`

Grafana dashboards to import:

- PostgreSQL Exporter: `9628`
- Redis Exporter: `763`

Grafana data source configuration:

- Type: `Prometheus`
- URL: `http://prometheus:9090`

Stop monitoring stack:

```powershell
docker compose down
```

## CI

GitHub Actions workflow runs tests on pushes and pull requests for `main`, `develop`, and `feature/**` branches.

## License

This project is licensed under the MIT License. See `LICENSE`.

