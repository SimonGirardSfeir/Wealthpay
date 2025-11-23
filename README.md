# WealthPay — Modular Account Domain (DDD + Event Sourcing)

WealthPay is a personal side project focused on designing and implementing a clean and extensible **banking-grade account domain**, using modern architectural practices such as **Domain-Driven Design**, **Event Sourcing**, **CQRS**, and **Modular Monolith** principles with Spring Modulith.

The goal is to build a fully consistent and testable domain for account operations (open, credit, debit, reserve funds, cancel reservation, close account) using techniques common in real financial systems.

---

## 🏗 Architecture

### ✔ Domain-Driven Design (DDD)
- Explicit domain model (aggregate, value objects, domain invariants)
- Dedicated domain exceptions with meaningful semantics
- Commands and events as the main input/output of the aggregate

### ✔ Event Sourcing
- Every state change is captured as an immutable domain event
- Aggregate state is rebuilt through event replay (`rehydrate`)
- Event store backed by **PostgreSQL** (`event_store` table + JSONB payloads)
- Snapshot support planned for large histories

### ✔ CQRS
- Commands mutate state via events
- Queries rely on read projections (to be introduced later)

### ✔ Hexagonal Architecture
- Domain is isolated from infrastructure
- Application services orchestrate operations
- Infrastructure adapters: REST controllers, JOOQ persistence, mappers, configs

### ✔ Modular Monolith with Spring Modulith
- `account` is a standalone, closed module
- `shared` contains cross-cutting concerns (clock, global error handling)
- Module boundaries are enforced via architecture tests

---

## 💾 Persistence Layer

### ✔ PostgreSQL (Dockerized)
- Local development uses `docker-compose`
- Schema managed via Flyway migrations
- Event store modeled with `JSONB` payloads and versioning

### ✔ JOOQ for type-safe SQL
- Explicit control of queries
- Fine-grained mapping for event serialization/deserialization

---

---

## 🛠 Local Development Workflow

This project uses PostgreSQL (via `docker-compose`), Flyway (automatic schema migrations), and jOOQ (type-safe SQL with code generation).

Follow this workflow when you clone the project or when database changes occur.

### 1. Start PostgreSQL (Docker)

Use the provided `docker-compose.yml`:

```bash
docker-compose up -d
```

### 2. Apply Flyway migrations

Flyway is executed automatically when Spring Boot starts.

Run the application once:

```bash
mvn spring-boot:run
```

This will:
•	connect to the local PostgreSQL instance
•	apply all Flyway migrations
•	create/update the account schema

You can stop the application once the startup completes.

### 3. Generate jOOQ classes

(only when the database schema changes)

jOOQ code generation is not part of the default Maven lifecycle because it requires a live PostgreSQL database.

After applying new Flyway migrations, regenerate the jOOQ classes with:

```bash
mvn -Pjooq-codegen-local clean generate-sources
```

This updates the generated sources under:

```bash
src/main/generated-jooq/
```

These files are versioned so that CI and other developers can build the project without needing to run jOOQ codegen.

### 4. Build the project

Once the jOOQ sources exist (generated locally or pulled from Git):

```bash
mvn clean install
```

No running database is required for this step.


## 🌐 REST API

The contract is defined **OpenAPI-first**, and DTOs/interfaces are code-generated using OpenAPI Generator.

Error handling:
- Global validation errors (`400`)
- Domain rule violations (`422`)
- Resource conflicts (`409`)
- Missing resources (`404`)
- Internal inconsistencies (`500`)

---

## 🧪 Testing Strategy

- **TDD** applied to the entire domain (commands, events, invariants)
- Application service tests with mocked event store
- Web layer tested using `@WebMvcTest`
- Architecture rules enforced with Spring Modulith tests
- Integration tests using real PostgreSQL via Testcontainers

---

## 🎯 Project Goals

This project is both:
- A **technical playground** to push clean design and strong architectural discipline
- A **realistic financial domain** (similar to private banking account engines)
- A way to demonstrate proficiency with advanced backend concepts:
    - DDD & tactical patterns
    - Event Sourcing & consistency handling
    - Hexagonal + modular monolith
    - Spring Boot 3.3+, JOOQ, Flyway, Postgres
    - OpenAPI contract-first API design
    - Strong testing practices

---