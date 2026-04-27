# Mini iFood API

Mini iFood API is a portfolio-ready REST backend built with Java 21 and Spring Boot.
It demonstrates a production-style stack with JWT security, PostgreSQL, Redis caching,
Flyway migrations, automated tests, and Docker-based observability.

## Why this project stands out

This repository was designed to show the kind of backend work expected in a real product team:

- Clean separation by domain (`auth`, `user`, `product`, `order`, `payment`, `security`, `config`)
- Stateless authentication with Spring Security + JWT
- Real persistence with PostgreSQL and Flyway
- Product listing cache backed by Redis
- Tests across unit, integration, security, and HTTP layers
- Monitoring with Prometheus and Grafana
- One-command local startup with Docker Compose

## Tech stack

- Java 21
- Spring Boot 4.0.4
- Spring Security + JWT
- Spring Data JPA + Hibernate
- Flyway
- PostgreSQL
- Redis
- Micrometer + Prometheus
- Grafana
- OpenAPI / Swagger
- Maven
- Docker / Docker Compose

## Core features

- Register and login users
- Issue and validate JWT tokens
- CRUD for users and products
- Product pagination and filtering
- Order creation, listing, status flow, and cancellation rules
- Role-based authorization for admin actions
- Mock payment flow connected to order status
- Redis cache for product listing
- Production-style observability endpoints

## Login and security flow

1. `POST /api/auth/register` creates a user.
2. `POST /api/auth/login` authenticates credentials and returns a JWT.
3. `SecurityConfig` keeps the app stateless and registers the JWT filter.
4. Public routes include:
   - `/api/auth/**`
   - `/v3/api-docs/**`
   - `/swagger-ui/**`
   - `/swagger-ui.html`
   - `/actuator/health/**`
   - `/actuator/info`
   - `/actuator/prometheus`
5. All other endpoints require authentication.
6. Method security is enabled for role-based checks when needed.

## API documentation

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

The controllers and DTOs are documented with Springdoc annotations such as `@Operation`,
`@ApiResponse`, and `@Schema`.

## Project structure

```text
src/main/java/com/marcosdias/miniifood/
  auth/       Authentication (login/register)
  user/       User management
  product/    Product CRUD and cache integration
  order/      Order aggregate and status rules
  payment/    Mock payment flow
  security/   JWT service, filter, and security configuration
  config/     Cache and infrastructure configuration
```

## Testing strategy

The repository includes real tests, not placeholders:

- Unit tests for service logic
- Integration tests for repositories and services
- Security tests for JWT, authentication, and access control
- HTTP tests with `MockMvc` for auth and product endpoints
- HTTP integration tests for order endpoints
- Optional Testcontainers smoke test for PostgreSQL and Redis

Useful commands:

```powershell
.\mvnw.cmd test
.\mvnw.cmd test -Dtest=AuthControllerMvcTest,ProductControllerMvcTest
.\mvnw.cmd test -Dtest=OrderControllerIntegrationTest
```

### Optional Testcontainers run

```powershell
$env:RUN_TESTCONTAINERS="true"
.\mvnw.cmd test -Dtest=ContainersSmokeTest
```

## Cache behavior

The product listing is cached in Redis with a 60-minute TTL.
When products are created, updated, or deleted, the cache is evicted so the next request rebuilds it.

Helpful command during local testing:

```powershell
docker exec -it mini-ifood-cache redis-cli FLUSHALL
```

## Observability

Metrics are exposed at:

```text
http://localhost:8080/actuator/prometheus
```

The Docker Compose stack also includes:

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

Prometheus scrapes:

- `app:8080`
- `postgres-exporter:9187`
- `redis-exporter:9121`

Suggested Grafana dashboards:

- PostgreSQL Exporter: `9628`
- Redis Exporter: `763`

Grafana data source:

- Type: `Prometheus`
- URL: `http://prometheus:9090`

## Run locally with Docker

Requirements:

- JDK 21+
- Docker Desktop
- Docker Compose

1. Copy the example environment file:

```powershell
Copy-Item .env.example .env
```

2. Start the full stack:

```powershell
docker compose up -d --build
```

3. Verify the services:

```powershell
docker compose ps
```

4. Open the main URLs:

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

Default Grafana login:

- user: `admin`
- password: `admin`

Stop the stack:

```powershell
docker compose down
```

## Environment variables

The project supports these variables from `.env.example`:

```dotenv
SPRING_PROFILES_ACTIVE=dev
JWT_SECRET=change-this-secret-key-with-at-least-32-characters
JWT_EXPIRATION_MINUTES=60
DB_HOST=postgres
DB_PORT=5432
DB_NAME=mini_ifood
DB_USER=postgres
DB_PASSWORD=postgres
REDIS_HOST=redis
REDIS_PORT=6379
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
```

## Profiles

- `dev`: PostgreSQL + Redis
- `test`: H2 in-memory for fast tests
- `prod`: strict PostgreSQL runtime config
- `tc`: Testcontainers support

## Postman collection

Import `MiniIfoodAPI.postman_collection.json` to test the API quickly.

Recommended flow:

1. Register a user.
2. Login and copy the JWT token.
3. Call protected endpoints with the `Authorization: Bearer <token>` header.
4. Run the product cache requests twice to see the Redis hit.

## License

This project is licensed under the MIT License. See `LICENSE`.

