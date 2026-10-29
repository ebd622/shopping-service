# Research: Expose Shopping APIs

## Decision 1: Order cancellation model

- Decision: Use soft cancel for `DELETE /api/v1/orders/{id}` by transitioning order state to
  `CANCELED` instead of physically deleting records.
- Rationale: Preserves auditability, keeps retrieval semantics stable, and aligns with clarified
  spec behavior that canceled orders remain retrievable.
- Alternatives considered:
  - Hard delete: rejected because it breaks traceability and complicates duplicate/retry handling.
  - Time-window retention then purge: rejected for v1 to avoid policy complexity in initial scope.

## Decision 2: POST idempotency strategy

- Decision: Require an idempotency key for `POST /api/v1/orders`; repeated requests with identical
  key and equivalent payload return the original successful result.
- Rationale: Prevents duplicate orders during client/network retries and supports deterministic
  client behavior.
- Alternatives considered:
  - No deduplication: rejected due to duplicate order risk.
  - Heuristic deduplication by payload/time window: rejected due to false positives/negatives.

## Decision 3: DELETE idempotency and conflict behavior

- Decision: Repeated DELETE on already canceled orders returns success as a no-op; DELETE on
  non-cancellable states returns standardized conflict.
- Rationale: Matches HTTP idempotency expectations while preserving explicit lifecycle enforcement.
- Alternatives considered:
  - Return conflict for already canceled orders: rejected; breaks retry-friendly behavior.
  - Return not found for canceled orders: rejected; conflicts with soft-cancel retrievability.

## Decision 4: Standardized error response contract

- Decision: Use one canonical error envelope for validation, not-found, conflict, and transient
  upstream failures.
- Rationale: Constitution requires standardized responses and predictable client integration.
- Alternatives considered:
  - Endpoint-specific error formats: rejected due to inconsistency.
  - Framework-default errors only: rejected due to unstable public contract.

## Decision 5: API contract documentation approach

- Decision: Define OpenAPI 3 contract in `contracts/openapi.yaml` and keep endpoint examples for
  success/error cases.
- Rationale: Constitution mandates OpenAPI 3 and error example coverage for changed endpoints.
- Alternatives considered:
  - Code-first-only without explicit contract artifact: rejected for planning and review clarity.

## Decision 6: Observability baseline

- Decision: Include Actuator health/readiness endpoints and Micrometer metrics for endpoint latency,
  error counts, and order lifecycle events.
- Rationale: Supports production diagnostics and validates constitution observability requirements.
- Alternatives considered:
  - Minimal health endpoint only: rejected due to limited operational insight.

## Decision 7: Serialization baseline

- Decision: Standardize JSON serialization/deserialization on Jackson 3 only.
- Rationale: Required by constitution; avoids introducing prohibited Jackson 2 dependencies.
- Alternatives considered:
  - Jackson 2 compatibility layer: rejected as policy violation.

## Decision 8: Missing idempotency key handling

- Decision: Reject `POST /api/v1/orders` requests missing `Idempotency-Key` with a standardized
  validation error that identifies the required header.
- Rationale: Keeps request semantics deterministic, enforces retry safety contract, and aligns with
  clarified acceptance behavior.
- Alternatives considered:
  - Accept without key: rejected due to duplicate order risk.
  - Auto-generate server key: rejected because it hides client retry intent and weakens contract
    transparency.

## Decision 9: Product uniqueness strategy

- Decision: Use `sku` as the unique catalog identifier for product creation.
- Rationale: `sku` is stable for business/catalog workflows and gives deterministic duplicate
  detection semantics.
- Alternatives considered:
  - Name-based uniqueness: rejected because names are mutable and collision-prone.
  - Generated-id-only uniqueness: rejected because it allows duplicate business products.

## Decision 10: Required fields for product creation

- Decision: Require `sku`, `name`, and `price` in `POST /api/v1/products`.
- Rationale: These fields are the minimal set needed to identify and sell a product while keeping
  request payloads concise.
- Alternatives considered:
  - Require additional fields (`currency`, `description`): rejected for v1 simplicity.
  - Require only `sku`/`name`: rejected because purchasable price would be missing.

## Decision 11: Malformed order ID handling

- Decision: Treat malformed order IDs in `GET/DELETE /api/v1/orders/{id}` as validation errors.
- Rationale: Separates invalid input from not-found resources and yields clearer client remediation.
- Alternatives considered:
  - Return not found for malformed IDs: rejected because it conflates two distinct error classes.

## Decision 12: Transient catalog dependency failure behavior

- Decision: `POST /api/v1/orders` returns standardized service unavailable when required catalog
  data is temporarily unavailable; response is explicitly retryable.
- Rationale: Prevents creating orders with unverifiable product data while preserving safe retry
  semantics.
- Alternatives considered:
  - Accept then validate asynchronously: rejected for v1 due to consistency/reconciliation risk.
  - Return validation error: rejected because upstream outage is transient, not client input fault.

## Decision 13: Explicit non-cancellable states

- Decision: Treat `SHIPPED` and `COMPLETED` orders as non-cancellable.
- Rationale: Matches lifecycle expectations where fulfillment completion blocks cancellation.
- Alternatives considered:
  - Only `COMPLETED` non-cancellable: rejected as too permissive post-shipment.
  - Include `PAID` as non-cancellable: rejected for v1 to retain user flexibility before shipment.

