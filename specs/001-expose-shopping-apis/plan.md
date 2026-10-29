# Implementation Plan: Expose Shopping APIs

**Branch**: `001-expose-shopping-apis` | **Date**: 2026-10-29 | **Spec**: `specs/001-expose-shopping-apis/spec.md`

**Input**: Feature specification from `specs/001-expose-shopping-apis/spec.md`

## Summary

Expose versioned shopping endpoints for product listing, product creation, and order lifecycle
operations: list products, add products, create order, fetch order by ID, and cancel order with
soft-cancel semantics. The implementation prioritizes standardized error responses, request
validation, idempotent order creation with idempotency keys, retryable service-unavailable behavior
for transient catalog outages, and lifecycle-safe cancellation behavior aligned with the
constitution.

## Technical Context

**Language/Version**: Java 21 LTS

**Primary Dependencies**: Spring Boot 4.1.2, Spring Web, Spring Validation,
Spring Data JPA, PostgreSQL driver, Springdoc OpenAPI 3, Jackson 3, Spring Boot Actuator,
Micrometer

**Storage**: PostgreSQL

**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers (PostgreSQL)

**Target Platform**: Linux container runtime (Docker) with JVM 21

**Project Type**: RESTful web service

**Performance Goals**: p95 < 300 ms for read endpoints and p95 < 500 ms for order creation/delete
under expected baseline load

**Constraints**: API versioning under `/api/v1`; standardized error schema for all client-visible
errors; `POST /api/v1/products` requires `sku`, `name`, and `price`; duplicate `sku` must return
conflict; malformed order IDs return validation errors; soft-cancel orders remain retrievable;
DELETE idempotency for already canceled orders; non-cancellable statuses are `SHIPPED` and
`COMPLETED`; transient product-catalog dependency failures during order creation return retryable
service unavailable responses; no hard-coded secrets

**Scale/Scope**: Initial scope limited to five endpoints and core order lifecycle for demo service;
single service and single primary PostgreSQL database

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Design Gate Check

- PASS: Java 21 LTS, Maven, Spring Boot 4.1.2 baseline defined.
- PASS: API versioning and OpenAPI 3 documentation included in scope.
- PASS: PostgreSQL selected as primary database.
- PASS: Testing stack includes JUnit 5, Mockito, Testcontainers.
- PASS: Validation and standardized error responses are explicit requirements.
- PASS: Observability requirements include Actuator + Micrometer.
- PASS: Docker-ready runtime and externalized configuration acknowledged.
- PASS: Jackson 3 required, Jackson 2 prohibited.

### Post-Design Gate Check

- PASS: Data model supports soft-cancel lifecycle and non-cancellable conflict handling.
- PASS: Contract defines `/api/v1` routes, request/response validation, and error schema.
- PASS: Contract includes product creation request validation (`sku`, `name`, `price`) and
  duplicate-`sku` conflict behavior.
- PASS: Contract captures malformed order ID validation and retryable service-unavailable behavior
  for order creation when catalog data is unavailable.
- PASS: Quickstart validates functional, edge-case, and idempotency flows.
- PASS: No constitution violations introduced by planned design artifacts.

## Project Structure

### Documentation (this feature)

```text
specs/001-expose-shopping-apis/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
pom.xml
Dockerfile
src/
├── main/
│   ├── java/com/example/shopping/
│   │   ├── products/
│   │   │   ├── api/
│   │   │   ├── domain/
│   │   │   └── infra/
│   │   ├── orders/
│   │   │   ├── api/
│   │   │   ├── domain/
│   │   │   └── infra/
│   │   ├── shared/error/
│   │   └── shared/config/
│   └── resources/
│       ├── application.yml
│       └── db/migration/
└── test/
    ├── java/com/example/shopping/
    │   ├── unit/
    │   ├── integration/
    │   └── contract/
    └── resources/
```

**Structure Decision**: Single Maven-based Spring Boot service using feature-based packages
(`products`, `orders`) with shared cross-cutting modules for error handling and configuration.
This directly satisfies constitution mandates for architecture consistency and maintainability.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
