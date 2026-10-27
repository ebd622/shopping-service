# Quickstart: Expose Shopping APIs

## Prerequisites

- Java 21 LTS
- Maven 3.9+
- Docker (for local PostgreSQL/Testcontainers)
- Environment variables for DB credentials (no hard-coded secrets)

## 1) Prepare environment

```bash
cp .env.example .env
```

Populate `.env` with local values for PostgreSQL connection and application secrets.

## 2) Start dependencies

```bash
docker compose up -d postgres
```

## 3) Build and run tests

```bash
mvn clean test
```

Expected outcome:
- Unit tests pass (validation, service logic, error mapping)
- Integration tests pass using Testcontainers/PostgreSQL

## 4) Run service locally

```bash
mvn spring-boot:run
```

## 5) Validate endpoint flows

### List products

```bash
curl -sS http://localhost:8080/api/v1/products
```

Expected outcome:
- `200 OK` with product array (possibly empty)

### Create order (with idempotency key)

```bash
curl -sS -X POST http://localhost:8080/api/v1/orders \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-key-001' \
  -d '{"items":[{"productId":"11111111-1111-1111-1111-111111111111","quantity":1}]}'
```

Expected outcome:
- `201 Created` with `orderId`

### Retry create order using same idempotency key

```bash
curl -sS -X POST http://localhost:8080/api/v1/orders \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-key-001' \
  -d '{"items":[{"productId":"11111111-1111-1111-1111-111111111111","quantity":1}]}'
```

Expected outcome:
- Returns original order result; no duplicate order created

### Get order by ID

```bash
curl -sS http://localhost:8080/api/v1/orders/{id}
```

Expected outcome:
- `200 OK` with order details and status

### Cancel order

```bash
curl -sS -X DELETE http://localhost:8080/api/v1/orders/{id}
```

Expected outcome:
- `200 OK` (or `204 No Content` if chosen in implementation) and order transitions to `CANCELED`

### Repeat cancel

```bash
curl -sS -X DELETE http://localhost:8080/api/v1/orders/{id}
```

Expected outcome:
- Successful no-op response (idempotent)

### Non-cancellable state cancel

Expected outcome:
- Standardized conflict response

## Contract and model references

- API contract: `specs/001-expose-shopping-apis/contracts/openapi.yaml`
- Data model: `specs/001-expose-shopping-apis/data-model.md`

