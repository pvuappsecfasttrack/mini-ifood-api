# Mini iFood API

REST API for order management with Spring Boot, JWT authentication, and PostgreSQL.

## Setup

**Requirements**: JDK 21+, Maven 3.9+, PostgreSQL 15+

**Clone**:
```bash
git clone https://github.com/203marcos/mini-ifood-api.git
cd mini-ifood-api
```

**Environment Variables** (PowerShell):
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="5433"
$env:DB_NAME="mini_ifood"
$env:DB_USER="postgres"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your-secret-key-min-32-chars"
```

**Run**:
```bash
.\mvnw.cmd test          # Run tests
.\mvnw.cmd spring-boot:run  # Start app (http://localhost:8080)
```

## API

**Public**:
```
POST /api/auth/register
POST /api/auth/login
```

**Protected** (Bearer token required):
```
GET/POST/PUT/DELETE /api/users
GET/POST/PUT/DELETE /api/products?page=0&size=10
```

See Swagger: `http://localhost:8080/swagger-ui.html`

## Stack

| Component | Version |
|-----------|---------|
| Java | 21 |
| Spring Boot | 4.0.4 |
| PostgreSQL | 15+ |
| JWT | JJWT 0.12.6 |
| Build | Maven 3.9+ |

## Architecture

```
src/main/java/com/marcosdias/miniifood/
├── auth/       Authentication & login
├── security/   JWT & Spring Security
├── product/    Product CRUD
├── user/       User management
└── MiniIfoodApiApplication.java
```

## Features

- [x] User registration & JWT login
- [x] Spring Security + BCrypt
- [x] Product CRUD with pagination
- [x] PostgreSQL + Flyway migrations
- [x] Swagger/OpenAPI documentation
- [x] Unit & integration tests (23 tests)
- [x] GitHub Actions CI/CD
- [ ] Orders (Phase 6)
- [ ] Payments (Phase 8)
- [ ] Redis cache (Phase 9)
- [ ] Docker (Phase 11)

## Testing

```bash
.\mvnw.cmd test                    # All tests
.\mvnw.cmd test -Dtest=AuthServiceTest  # Single class
```

## Development

**Profiles**: `dev` (PostgreSQL) | `test` (H2) | `prod` (PostgreSQL strict)

**Key technologies**:
- Lombok (reduced boilerplate)
- Swagger annotations (API documentation)
- SLF4J logging
- Spring Data JPA + Hibernate

## License

MIT — See [LICENSE](LICENSE)

---

Marcos | [GitHub](https://github.com/203marcos) | March 2026

