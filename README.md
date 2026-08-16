# Spring Boot Project API

A Spring Boot REST API built with Java 21, Spring Data JPA, Postgres, and springdoc-openapi
(Swagger UI).

## Tech stack

- Java 21
- Spring Boot 4.0.8-SNAPSHOT
- Spring Data JPA + Postgres
- Bean Validation (jakarta.validation)
- springdoc-openapi (Swagger UI)
- Lombok
- Spring security
- Redis

## Getting started

Clone the repository, then detach it from this repo's git history so you can start your own:

```bash
git clone <repo-url> spring_boot_project_api
cd spring_boot_project_api
rm -rf .git
git init
docker compose up -d --build
```

### Prerequisites

- JDK 21
- Posgres running locally (or update `src/main/resources/application.properties` to point at
  your instance)

### Configure the database

Add your Postgres connection details to `src/main/resources/application.properties`:

```properties
# database connection detail
spring.datasource.url=jdbc:postgresql://postgres:5432/tourism_db
spring.datasource.username=postgres
spring.datasource.password=tourism

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Run

```bash
docker logs -f tourism-app
```

The API will be available at `http://localhost:8080`, and Swagger UI at
`http://localhost:8080/swagger-ui.html`.

### Test

```bash
./mvnw test
```

## Project structure

See [`agent_guide_ai.md`](agent_guide_ai.md) for the full folder layout and coding conventions
(controller → service → repository layering, DTOs, mappers, etc.). That file is also the shared
source of truth read by AI coding agents (Claude Code, Codex, opencode, Copilot, Antigravity).
