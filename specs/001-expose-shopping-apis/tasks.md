# Tasks: Expose Shopping APIs

**Input**: Design documents from `specs/001-expose-shopping-apis/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/openapi.yaml`, `quickstart.md`

**Tests**: Included because `spec.md` has mandatory testing scenarios and the constitution requires tests for behavior changes.

**Organization**: Tasks are grouped by user story for independent implementation and validation.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Parallelizable task (independent files, no incomplete dependency)
- **[Story]**: Story label for user-story phases (`[US1]`, `[US2]`, `[US3]`, `[US4]`)
- Every task includes an exact file path

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm baseline project setup and feature scaffolding.

- [X] T001 Verify Java 21, Spring Boot 4.1.2, Jackson 3, PostgreSQL, OpenAPI, Actuator, Micrometer, JUnit 5, Mockito, and Testcontainers dependencies in `pom.xml`
- [X] T002 Verify container/runtime assets for local and CI use in `Dockerfile`, `docker-compose.yml`, and `.dockerignore`
- [X] T003 [P] Verify environment placeholders and defaults for local profiles in `.env.example` and `src/main/resources/application.yml`
- [X] T004 [P] Verify feature package structure for `products`, `orders`, and `shared` modules under `src/main/java/com/example/shopping/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Implement or align cross-cutting infrastructure that blocks all user stories.

**CRITICAL**: Complete this phase before story work.

- [X] T005 Align schema and constraints for products/orders/idempotency in `src/main/resources/db/migration/V1__init_schema.sql`
- [X] T006 [P] Implement standardized error envelope (`code`, `message`, `details`, `timestamp`, `path`, `traceId`) in `src/main/java/com/example/shopping/shared/error/ApiErrorResponse.java`
- [X] T007 [P] Implement global exception mapping for validation, not-found, conflict, and service-unavailable responses in `src/main/java/com/example/shopping/shared/error/GlobalExceptionHandler.java`
- [X] T008 [P] Align OpenAPI metadata and shared error schemas in `src/main/java/com/example/shopping/shared/config/OpenApiConfig.java`
- [X] T009 [P] Add observability baseline for latency/error metrics and order lifecycle counters in `src/main/java/com/example/shopping/shared/config/ObservabilityConfig.java`
- [X] T010 [P] Align persistence and auditing configuration for PostgreSQL in `src/main/java/com/example/shopping/shared/config/PersistenceConfig.java`
- [X] T011 Add shared PostgreSQL Testcontainers base support in `src/test/java/com/example/shopping/integration/support/PostgresContainerSupport.java`

**Checkpoint**: Foundation complete; user stories can proceed.

---

## Phase 3: User Story 1 - Browse Product Catalog (Priority: P1) 🎯 MVP

**Goal**: Provide `GET /api/v1/products` that returns product arrays (including empty arrays) with standardized error behavior.

**Independent Test**: Request `GET /api/v1/products`; expect `200` with an array payload that matches `contracts/openapi.yaml`.

### Tests for User Story 1

- [X] T012 [P] [US1] Add contract test for `GET /api/v1/products` success schema in `src/test/java/com/example/shopping/contract/ProductsContractTest.java`
- [X] T013 [P] [US1] Add integration tests for non-empty and empty product list responses in `src/test/java/com/example/shopping/integration/ProductsIntegrationTest.java`

### Implementation for User Story 1

- [X] T014 [P] [US1] Implement `ProductEntity` fields and constraints `id` "UUID, required, unique", `sku` "string, required, unique", `name` "string, required, 1..200", `description` "string, optional, <=2000", `price` "decimal, required, positive", `currency` "string, optional, ISO-4217, defaults by service policy", `available` "boolean, required", `updatedAt` "timestamp, required" in `src/main/java/com/example/shopping/products/infra/ProductEntity.java`
- [X] T015 [P] [US1] Implement product query repository in `src/main/java/com/example/shopping/products/infra/ProductRepository.java`
- [X] T016 [P] [US1] Implement product response DTO in `src/main/java/com/example/shopping/products/api/ProductResponse.java`
- [X] T017 [US1] Implement product list service in `src/main/java/com/example/shopping/products/domain/ProductQueryService.java`
- [X] T018 [US1] Implement `GET /api/v1/products` endpoint in `src/main/java/com/example/shopping/products/api/ProductsController.java`

**Checkpoint**: US1 independently functional and testable.

---

## Phase 4: User Story 2 - Add a Product to Catalog (Priority: P2)

**Goal**: Provide `POST /api/v1/products` with required fields and duplicate-`sku` conflict handling.

**Independent Test**: Submit valid payload with `sku`, `name`, `price`; expect successful creation; duplicate `sku` returns conflict; missing required fields return validation error.

### Tests for User Story 2

- [X] T019 [P] [US2] Add contract tests for `POST /api/v1/products` covering `201`, `422`, and `409` in `src/test/java/com/example/shopping/contract/ProductsContractTest.java`
- [X] T020 [P] [US2] Add integration tests for product create success, duplicate `sku` conflict, and missing `sku`/`name`/`price` validation errors in `src/test/java/com/example/shopping/integration/ProductsIntegrationTest.java`
- [X] T021 [P] [US2] Add unit tests for product create validation and duplicate detection in `src/test/java/com/example/shopping/unit/products/ProductCreationServiceTest.java`

### Implementation for User Story 2

- [X] T022 [P] [US2] Implement create-product request DTO with required `sku`, `name`, and `price` validation in `src/main/java/com/example/shopping/products/api/CreateProductRequest.java`
- [X] T023 [P] [US2] Add repository lookup for duplicate `sku` in `src/main/java/com/example/shopping/products/infra/ProductRepository.java`
- [X] T024 [US2] Implement product creation service enforcing required fields and duplicate-`sku` conflict behavior in `src/main/java/com/example/shopping/products/domain/ProductCreationService.java`
- [X] T025 [US2] Implement `POST /api/v1/products` endpoint wiring in `src/main/java/com/example/shopping/products/api/ProductsController.java`
- [X] T026 [US2] Map duplicate-`sku` domain exception to standardized conflict response in `src/main/java/com/example/shopping/shared/error/GlobalExceptionHandler.java`

**Checkpoint**: US1 and US2 independently functional and testable.

---

## Phase 5: User Story 3 - Place an Order (Priority: P3)

**Goal**: Provide `POST /api/v1/orders` with idempotency key enforcement, retry-safe deduplication, and retryable service-unavailable behavior on transient catalog dependency failure.

**Independent Test**: Valid order create returns success; repeated request with same `Idempotency-Key` and equivalent payload returns original result; missing key returns validation error; temporary catalog outage returns retryable service-unavailable response.

### Tests for User Story 3

- [X] T027 [P] [US3] Add contract tests for `POST /api/v1/orders` covering `201`, `422` (including missing `Idempotency-Key`), `409`, and `503` in `src/test/java/com/example/shopping/contract/OrdersCreateContractTest.java`
- [X] T028 [P] [US3] Add integration tests for create success, idempotent replay, missing-header validation, and transient catalog outage in `src/test/java/com/example/shopping/integration/OrdersCreateIntegrationTest.java`
- [X] T029 [P] [US3] Add unit tests for idempotency-key reuse and request hash matching rules in `src/test/java/com/example/shopping/unit/orders/OrderCreationServiceTest.java`

### Implementation for User Story 3

- [X] T030 [P] [US3] Implement `OrderEntity` fields and constraints `id` "UUID, required, unique", `status` "enum, required: CREATED, PROCESSING, SHIPPED, COMPLETED, CANCELED", `createdAt` "timestamp, required", `updatedAt` "timestamp, required", `canceledAt` "timestamp, optional", `currency` "string, required, ISO-4217", `totalAmount` "decimal, required, >= 0", `idempotencyKey` "string, required for create, unique with request fingerprint" in `src/main/java/com/example/shopping/orders/infra/OrderEntity.java`
- [X] T031 [P] [US3] Implement `OrderItemEntity` fields and constraints `id` "UUID, required, unique", `orderId` "UUID, required, FK -> Order.id", `productId` "UUID, required, FK -> Product.id", `quantity` "integer, required, >= 1", `unitPriceAmount` "decimal, required, positive", `lineAmount` "decimal, required, positive" and rule `lineAmount = unitPriceAmount * quantity` in `src/main/java/com/example/shopping/orders/infra/OrderItemEntity.java`
- [X] T032 [P] [US3] Implement `OrderIdempotencyEntity` fields and constraints `idempotencyKey` "string, required", `requestHash` "string, required", `orderId` "UUID, required", `createdAt` "timestamp, required" and rule `Reused idempotency key with different request hash returns conflict/error` in `src/main/java/com/example/shopping/orders/infra/OrderIdempotencyEntity.java`
- [X] T033 [P] [US3] Implement order repositories in `src/main/java/com/example/shopping/orders/infra/OrderRepository.java`, `src/main/java/com/example/shopping/orders/infra/OrderItemRepository.java`, and `src/main/java/com/example/shopping/orders/infra/OrderIdempotencyRepository.java`
- [X] T034 [P] [US3] Implement order request/response DTO validation (`items` min 1, `quantity >= 1`) in `src/main/java/com/example/shopping/orders/api/CreateOrderRequest.java` and `src/main/java/com/example/shopping/orders/api/OrderResponse.java`
- [X] T035 [US3] Implement order creation domain service with idempotency and request-fingerprint logic in `src/main/java/com/example/shopping/orders/domain/OrderCreationService.java`
- [X] T036 [US3] Implement transient catalog-unavailable handling with retryable exception in `src/main/java/com/example/shopping/orders/domain/OrderCreationService.java`
- [X] T037 [US3] Implement `POST /api/v1/orders` endpoint with mandatory `Idempotency-Key` in `src/main/java/com/example/shopping/orders/api/OrdersController.java`

**Checkpoint**: US3 independently functional and testable.

---

## Phase 6: User Story 4 - Track and Cancel an Order (Priority: P4)

**Goal**: Provide `GET /api/v1/orders/{id}` and `DELETE /api/v1/orders/{id}` with soft cancel, idempotent repeated delete, malformed-ID validation error, and conflict on non-cancellable states.

**Independent Test**: Retrieve by ID, cancel order, retrieve canceled order, repeat delete as no-op success, return conflict for `SHIPPED`/`COMPLETED`, and return validation error for malformed ID.

### Tests for User Story 4

- [X] T038 [P] [US4] Add contract tests for `GET/DELETE /api/v1/orders/{id}` covering `200`, `404`, `409`, and `422` in `src/test/java/com/example/shopping/contract/OrdersLifecycleContractTest.java`
- [X] T039 [P] [US4] Add integration tests for soft-cancel lifecycle, repeated-delete no-op, `SHIPPED`/`COMPLETED` conflict, and malformed ID validation in `src/test/java/com/example/shopping/integration/OrdersLifecycleIntegrationTest.java`
- [X] T040 [P] [US4] Add unit tests for cancellation transitions and non-cancellable rules in `src/test/java/com/example/shopping/unit/orders/OrderLifecycleServiceTest.java`

### Implementation for User Story 4

- [X] T041 [US4] Implement order query service with standardized not-found behavior in `src/main/java/com/example/shopping/orders/domain/OrderQueryService.java`
- [X] T042 [US4] Implement cancellation service transitions `CREATED -> CANCELED`, `PROCESSING -> CANCELED`, `CANCELED -> CANCELED` no-op, and conflict on `SHIPPED`/`COMPLETED` in `src/main/java/com/example/shopping/orders/domain/OrderCancellationService.java`
- [X] T043 [US4] Implement `GET /api/v1/orders/{id}` and `DELETE /api/v1/orders/{id}` endpoint wiring in `src/main/java/com/example/shopping/orders/api/OrdersController.java`
- [X] T044 [US4] Implement malformed UUID path handling as standardized validation errors in `src/main/java/com/example/shopping/orders/api/OrdersController.java` and `src/main/java/com/example/shopping/shared/error/GlobalExceptionHandler.java`

**Checkpoint**: All user stories independently functional and testable.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final consistency, documentation, and full verification.

- [X] T045 [P] Align all endpoint examples and error responses (`422`, `409`, `503`) in `specs/001-expose-shopping-apis/contracts/openapi.yaml`
- [X] T046 [P] Update runbook scenarios and expected outcomes in `specs/001-expose-shopping-apis/quickstart.md`
- [X] T047 [P] Align data rules and transitions in `specs/001-expose-shopping-apis/data-model.md`
- [X] T048 [P] Update repository usage/build/test guidance in `README.md`
- [X] T049 Execute full unit/integration/contract test suite and verify Maven build in `pom.xml`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies.
- **Phase 2 (Foundational)**: Depends on Phase 1; blocks all user stories.
- **Phase 3 (US1)**: Depends on Phase 2.
- **Phase 4 (US2)**: Depends on US1 catalog foundations.
- **Phase 5 (US3)**: Depends on Phase 2 and uses US1/US2 product capabilities.
- **Phase 6 (US4)**: Depends on US3 order domain.
- **Phase 7 (Polish)**: Depends on completion of desired stories.

### User Story Dependencies

- **US1 (P1)**: Independent after foundational phase.
- **US2 (P2)**: Depends on US1 product entity/repository baseline.
- **US3 (P3)**: Depends on product data availability from US1/US2.
- **US4 (P4)**: Depends on US3 order lifecycle entities/services.

### Within Each User Story

- Tests first (write and fail before implementation).
- Entities/DTOs before services.
- Services before controllers.
- Controller wiring before end-to-end verification.

### Parallel Opportunities

- Setup parallel tasks: `T003`, `T004`
- Foundational parallel tasks: `T006`, `T007`, `T008`, `T009`, `T010`
- US1 parallel tasks: `T012`, `T013`, `T014`, `T015`, `T016`
- US2 parallel tasks: `T019`, `T020`, `T021`, `T022`, `T023`
- US3 parallel tasks: `T027`, `T028`, `T029`, `T030`, `T031`, `T032`, `T033`, `T034`
- US4 parallel tasks: `T038`, `T039`, `T040`

---

## Parallel Example: User Story 1

```bash
Task: "T012 [US1] contract test in src/test/java/com/example/shopping/contract/ProductsContractTest.java"
Task: "T013 [US1] integration test in src/test/java/com/example/shopping/integration/ProductsIntegrationTest.java"
Task: "T014 [US1] ProductEntity in src/main/java/com/example/shopping/products/infra/ProductEntity.java"
Task: "T016 [US1] ProductResponse in src/main/java/com/example/shopping/products/api/ProductResponse.java"
```

## Parallel Example: User Story 2

```bash
Task: "T019 [US2] contract tests in src/test/java/com/example/shopping/contract/ProductsContractTest.java"
Task: "T020 [US2] integration tests in src/test/java/com/example/shopping/integration/ProductsIntegrationTest.java"
Task: "T021 [US2] unit tests in src/test/java/com/example/shopping/unit/products/ProductCreationServiceTest.java"
Task: "T022 [US2] request DTO in src/main/java/com/example/shopping/products/api/CreateProductRequest.java"
```

## Parallel Example: User Story 3

```bash
Task: "T027 [US3] contract tests in src/test/java/com/example/shopping/contract/OrdersCreateContractTest.java"
Task: "T028 [US3] integration tests in src/test/java/com/example/shopping/integration/OrdersCreateIntegrationTest.java"
Task: "T030 [US3] OrderEntity in src/main/java/com/example/shopping/orders/infra/OrderEntity.java"
Task: "T032 [US3] OrderIdempotencyEntity in src/main/java/com/example/shopping/orders/infra/OrderIdempotencyEntity.java"
```

## Parallel Example: User Story 4

```bash
Task: "T038 [US4] contract tests in src/test/java/com/example/shopping/contract/OrdersLifecycleContractTest.java"
Task: "T039 [US4] integration tests in src/test/java/com/example/shopping/integration/OrdersLifecycleIntegrationTest.java"
Task: "T040 [US4] unit tests in src/test/java/com/example/shopping/unit/orders/OrderLifecycleServiceTest.java"
```

---

## Implementation Strategy

### MVP First (US1)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 (US1).
3. Validate `GET /api/v1/products` independently against contract and integration tests.

### Incremental Delivery

1. Deliver US1 (browse catalog).
2. Deliver US2 (add product).
3. Deliver US3 (place order with idempotency).
4. Deliver US4 (track and cancel order).
5. Run polish and full-suite verification.

### Parallel Team Strategy

1. Team completes Setup + Foundational together.
2. Split US1/US2/US3 test and model work in parallel.
3. Finish US4 once US3 domain behavior is stable.

---

## Notes

- `[P]` tasks target independent files or isolated responsibilities.
- Keep all API responses aligned to standardized error envelope.
- Keep serialization on Jackson 3 only.
- Keep secrets externalized; do not hard-code credentials.
