# Mini iFood API

API REST completa para gerenciamento de pedidos baseada em uma arquitetura REST, com autenticação JWT, cache Redis e integração com PostgreSQL usando JPA e Hibernate. O projeto segue boas práticas como separação em camadas, testes automatizados e pipeline CI/CD.

---

## Objetivo

Demonstrar uma implementação profissional de uma API de pedidos estilo iFood, com segurança via JWT, controle de acesso, persistência relacional e automação com GitHub Actions. Ideal como portfólio técnico para vagas de desenvolvedor back-end Java/Spring.

---

## Tecnologias

| Tecnologia | Versão | Propósito |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 4.0.4 | Framework web |
| Spring Security | - | Autenticação JWT |
| Spring Data JPA | - | Persistência ORM |
| PostgreSQL | 15+ | Banco de dados relacional |
| Flyway | 10+ | Versionamento de schema |
| JWT (JJWT) | 0.12.6 | Tokens stateless |
| H2 | 2.4+ | Testes em memória |
| Maven | 3.9+ | Build & dependency |
| GitHub Actions | - | CI/CD automatizado |
| Docker | 24+ | Containerização (futuro) |

---

## Arquitetura

```
src/main/java/com/marcosdias/miniifood/
├── auth/                        # Autenticação e registro
│   ├── AuthController.java
│   ├── AuthService.java
│   └── dto/
├── security/                    # Segurança e JWT
│   ├── SecurityConfig.java
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── AppUserDetailsService.java
├── user/                        # Gestão de usuários
│   ├── domain/User.java
│   ├── repository/UserRepository.java
│   ├── service/UserService.java
│   ├── web/UserController.java
│   └── web/dto/
└── MiniIfoodApiApplication.java
```

---

## Funcionalidades Implementadas

### Fase 1-3: Setup + User + Database
- [x] Estrutura Spring Boot com Maven
- [x] Entidade User com JPA
- [x] CRUD completo de usuários
- [x] PostgreSQL + Flyway migrations
- [x] Ambiente com profiles (dev, test, prod)

### Fase 4: Segurança + JWT
- [x] Spring Security + BCrypt
- [x] Geração e validação de JWT
- [x] Endpoints /api/auth/register e /api/auth/login
- [x] Filtro JWT stateless
- [x] Proteção de rotas com Bearer Token

### Fase 5+: Produtos, Pedidos, Cache, Docker, Testes
- [ ] Fase 5: Product entity + CRUD + Pagination
- [ ] Fase 6: Order entity + OrderItem + Relacionamentos
- [ ] Fase 7: Order status flow
- [ ] Fase 8: Payment mock
- [ ] Fase 9: Redis cache
- [ ] Fase 10: Testes completos
- [ ] Fase 11: Docker + docker-compose
- [ ] Fase 12: Documentação

---

## Quick Start

### Pré-requisitos
- JDK 21+
- Maven 3.9+
- PostgreSQL 15+ (ou usar perfil dev com fallback)
- Git

### Instalação Local

1. Clone o repositório
   ```bash
   git clone https://github.com/203marcos/mini-ifood-api.git
   cd mini-ifood-api
   ```

2. Configure variáveis de ambiente (PowerShell)
   ```powershell
   $env:DB_HOST="localhost"
   $env:DB_PORT="5433"
   $env:DB_NAME="mini_ifood"
   $env:DB_USER="postgres"
   $env:DB_PASSWORD="seu_senha_postgres"
   $env:JWT_SECRET="sua-chave-secreta-com-32-caracteres-minimo"
   ```

3. Execute testes
   ```bash
   .\mvnw.cmd test
   ```

4. Rode a aplicação
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

   API estará em: http://localhost:8080

---

## API Endpoints

### Autenticação
- `POST /api/auth/register` — Registrar novo usuário
- `POST /api/auth/login` — Login e obter JWT token

### Usuários (protegido por Bearer Token)
- `GET /api/users` — Listar todos os usuários
- `GET /api/users/{id}` — Obter usuário por ID
- `PUT /api/users/{id}` — Atualizar usuário
- `DELETE /api/users/{id}` — Deletar usuário

### Exemplo de uso com cURL

```bash
# Registrar
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Marcos","email":"marcos@email.com","password":"123456"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"marcos@email.com","password":"123456"}'

# Usar token (copie o accessToken retornado)
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer seu_token_aqui"
```

---

## Testes

```bash
# Rodar todos os testes
.\mvnw.cmd test

# Rodar apenas testes de uma classe
.\mvnw.cmd test -Dtest=AuthServiceTest

# Rodar com relatório de cobertura (futuro)
.\mvnw.cmd test jacoco:report
```

Cobertura atual: 17 testes unitários e integração (100% das features Fase 1-4)

---

## Fluxo Git & CI/CD

### Estratégia de Branches
- `main` — Produção pronta, merges apenas de develop
- `develop` — Base para features, sempre testada
- `feature/*` — Desenvolvimento de features isoladas
- `fix/*` — Correções em produção

### GitHub Actions
Workflow `.github/workflows/ci.yml` executa automaticamente em:
- Push para main, develop ou feature/**
- Pull Request para main ou develop

Validações:
- Build com Maven
- Testes JUnit
- Lint (futuro: SonarQube)

---

## Notas Importantes

### Sobre a estrutura de CI/CD
- CI (Continuous Integration) agora: teste + build validados em cada push
- CD (Continuous Deployment) depois: ao integrar Docker/Kubernetes
- Considerar branch separada `feature/ci-cd` em futuras refatorações de pipeline

### Sobre perfis Spring
- `dev`: PostgreSQL com variáveis de ambiente, ideal para desenvolvimento local
- `test`: H2 em memória, apenas para testes JUnit (não precisa Postgres)
- `prod`: Postgres exigindo todas variáveis sem fallback, pronto para produção

---

## Segurança

- Senhas criptografadas com BCrypt (não armazenadas em plaintext)
- JWT tokens com expiração configurável
- CSRF desabilitado (API stateless)
- Validação de entrada com @Valid + Jakarta Bean Validation
- HTTPS/TLS (quando containerizado)
- Rate limiting (futuro)

---

## Build & Deploy

### Build local
```bash
.\mvnw.cmd clean package
```

Gera: target/mini-ifood-api-0.0.1-SNAPSHOT.jar

### Docker (próxima fase)
```bash
docker build -t mini-ifood-api:latest .
docker run -p 8080:8080 \
  -e DB_HOST=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=seu-secret \
  mini-ifood-api:latest
```

---

## Roadmap

| Fase | Status | Descrição |
|---|---|---|
| 1 | Concluída | Setup inicial + estrutura |
| 2 | Concluída | User entity + Repository + Service + Controller |
| 3 | Concluída | PostgreSQL + Flyway migrations |
| 4 | Concluída | Spring Security + JWT + Auth endpoints |
| 5 | Em progresso | Product entity + CRUD + Pagination |
| 6 | Planejada | Order entity + OrderItem + Relacionamentos |
| 7 | Planejada | Order status flow + validações |
| 8 | Planejada | Payment mock endpoint |
| 9 | Planejada | Redis cache |
| 10 | Planejada | Testes completos (unit + integration) |
| 11 | Planejada | Docker + docker-compose |
| 12 | Planejada | Documentação + cleanup |

---

## Documentação Adicional

- Swagger/OpenAPI disponível em /swagger-ui.html quando app está rodando
- Migrations em src/main/resources/db/migration/
- Perfis e configurações por ambiente em src/main/resources/application-*.yaml

---

## Contribuição

Este é um projeto pessoal de portfólio. Sugestões e feedback são bem-vindos.

---

## Licença

MIT License — veja [LICENSE](LICENSE) para detalhes.

---

## Autor

Marcos — [GitHub](https://github.com/203marcos)

Desenvolvido para demonstrar expertise em Spring Boot, segurança e arquitetura REST.

---

Última atualização: Março 2026

