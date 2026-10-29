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
curl -sS -v http://localhost:8080/api/v1/products
```

Expected outcome:
- `200 OK` with product array (possibly empty)

### Create product

```bash
curl -sS -X POST http://localhost:8080/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"SKU-1001","name":"Coffee Beans","price":12.99}'
```

Expected outcome:
- `201 Created` with product payload including unique `sku`

### Create duplicate product

```bash
curl -sS -X POST http://localhost:8080/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"SKU-1002","name":"Coffee Beans","price":12.99}'
```

Expected outcome:
- Standardized conflict error (duplicate `sku`)

### Create product with missing required field

```bash
curl -sS -X POST http://localhost:8080/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"SKU-1002","name":"Tea"}'
```

Expected outcome:
- Standardized validation error (`price` is required)

### Create order (with idempotency key)

```bash
curl -sS -v -X POST http://localhost:8080/api/v1/orders \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-key-001' \
  -d '{"items":[{"productId":"a68d9cba-9403-4731-ac2b-82b988404d29","quantity":1}]}'
```

Expected outcome:
- `201 Created` with `orderId`

### Retry create order using same idempotency key

```bash
curl -sS -v -X POST http://localhost:8080/api/v1/orders \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-key-001' \
  -d '{"items":[{"productId":"a68d9cba-9403-4731-ac2b-82b988404d29","quantity":1}]}'
```

Expected outcome:
- Returns original order result; no duplicate order created

### Create order without idempotency key

```bash
curl -sS -X POST http://localhost:8080/api/v1/orders \
  -H 'Content-Type: application/json' \
  -d '{"items":[{"productId":"11111111-1111-1111-1111-111111111111","quantity":1}]}'
```

Expected outcome:
- Standardized validation error indicating `Idempotency-Key` is required

### Get order by ID

```bash
curl -sS http://localhost:8080/api/v1/orders/{id}
```

Expected outcome:
- `200 OK` with order details and status

### Get order with malformed ID

```bash
curl -sS http://localhost:8080/api/v1/orders/not-a-uuid
```

Expected outcome:
- Standardized validation error (malformed order ID)

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
- Standardized conflict response when order status is `SHIPPED` or `COMPLETED`

### Order creation when catalog dependency is unavailable

Expected outcome:
- Standardized service unavailable error marked retryable

## Contract and model references

- API contract: `specs/001-expose-shopping-apis/contracts/openapi.yaml`
- Data model: `specs/001-expose-shopping-apis/data-model.md`

