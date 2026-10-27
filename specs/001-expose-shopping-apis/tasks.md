# Tasks: Expose Shopping APIs

**Input**: Design documents from `specs/001-expose-shopping-apis/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/openapi.yaml`

**Tests**: Included because the constitution requires JUnit 5, Mockito, and Testcontainers coverage for behavior changes.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on incomplete tasks)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`) for story-specific phases
- Every task includes an exact file path

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize project skeleton and tooling baseline.

- [ ] T001 Create Maven project descriptor with Java 21, Spring Boot, PostgreSQL, Springdoc, Actuator, Micrometer, JUnit 5, Mockito, and Testcontainers dependencies in `pom.xml`
- [ ] T002 Create application bootstrap class in `src/main/java/com/example/shopping/ShoppingServiceApplication.java`
- [ ] T003 [P] Create Docker image build definition for the service in `Dockerfile`
- [ ] T004 [P] Create local dependency orchestration for PostgreSQL in `docker-compose.yml`
- [ ] T005 [P] Create baseline environment-aware configuration (no hard-coded secrets) in `src/main/resources/application.yml`
- [ ] T006 [P] Create package scaffolding for feature-based modules and shared components under `src/main/java/com/example/shopping/products/`, `src/main/java/com/example/shopping/orders/`, `src/main/java/com/example/shopping/shared/error/`, and `src/main/java/com/example/shopping/shared/config/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Implement core infrastructure that blocks all user stories.

**⚠️ CRITICAL**: No user story implementation should start before this phase is complete.

- [ ] T007 Create initial database migration for `products`, `orders`, `order_items`, and `order_idempotency` tables in `src/main/resources/db/migration/V1__init_schema.sql`
- [ ] T008 [P] Implement standardized API error envelope (`code`, `message`, `details`, `timestamp`, `path`, `traceId`) in `src/main/java/com/example/shopping/shared/error/ApiErrorResponse.java`
- [ ] T009 [P] Implement global exception handling for validation/not-found/conflict/server errors in `src/main/java/com/example/shopping/shared/error/GlobalExceptionHandler.java`
- [ ] T010 [P] Implement OpenAPI configuration with API v1 metadata and standardized error schema registration in `src/main/java/com/example/shopping/shared/config/OpenApiConfig.java`
- [ ] T011 [P] Implement observability baseline (Actuator exposure, Micrometer tags/timers) in `src/main/java/com/example/shopping/shared/config/ObservabilityConfig.java`
- [ ] T012 Implement JPA auditing/time provider configuration for created/updated timestamps in `src/main/java/com/example/shopping/shared/config/PersistenceConfig.java`

**Checkpoint**: Foundation complete; user story phases can proceed.

---

## Phase 3: User Story 1 - Browse Product Catalog (Priority: P1) 🎯 MVP

**Goal**: Provide `GET /api/v1/products` with valid product payloads and standardized errors.

**Independent Test**: Calling `GET /api/v1/products` returns `200` and a valid product array (possibly empty), with schema matching `contracts/openapi.yaml`.

### Tests for User Story 1

- [ ] T013 [P] [US1] Add contract test for `GET /api/v1/products` response schema in `src/test/java/com/example/shopping/contract/ProductsContractTest.java`
- [ ] T014 [P] [US1] Add integration test for list products success and empty catalog scenarios in `src/test/java/com/example/shopping/integration/ProductsIntegrationTest.java`

### Implementation for User Story 1

- [ ] T015 [P] [US1] Create `ProductEntity` with constraints from data model: `id` "UUID, required, unique", `sku` "string, required, unique", `name` "string, required, 1..200", `description` "string, optional, <=2000", `priceAmount` "decimal, required, positive", `priceCurrency` "string, required, ISO-4217", `available` "boolean, required", `updatedAt` "timestamp, required" in `src/main/java/com/example/shopping/products/infra/ProductEntity.java`
- [ ] T016 [P] [US1] Create product DTO as Java Record for API responses in `src/main/java/com/example/shopping/products/api/ProductResponse.java`
- [ ] T017 [P] [US1] Implement product repository access in `src/main/java/com/example/shopping/products/infra/ProductRepository.java`
- [ ] T018 [US1] Implement product listing application service in `src/main/java/com/example/shopping/products/domain/ProductQueryService.java`
- [ ] T019 [US1] Implement `GET /api/v1/products` controller endpoint and response mapping in `src/main/java/com/example/shopping/products/api/ProductsController.java`

**Checkpoint**: User Story 1 is independently functional and testable.

---

## Phase 4: User Story 2 - Place an Order (Priority: P2)

**Goal**: Provide `POST /api/v1/orders` with validation, idempotency key semantics, and order creation.

**Independent Test**: Valid create request with `Idempotency-Key` returns created order; retry with same key and equivalent payload returns original result without duplicate order.

### Tests for User Story 2

- [ ] T020 [P] [US2] Add contract tests for `POST /api/v1/orders` success, validation error, and conflict responses in `src/test/java/com/example/shopping/contract/OrdersCreateContractTest.java`
- [ ] T021 [P] [US2] Add integration tests for create-order and idempotent retry behavior with PostgreSQL in `src/test/java/com/example/shopping/integration/OrdersCreateIntegrationTest.java`
- [ ] T022 [P] [US2] Add unit tests for order creation/idempotency service logic with Mockito in `src/test/java/com/example/shopping/unit/orders/OrderCreationServiceTest.java`

### Implementation for User Story 2

- [ ] T023 [P] [US2] Create `OrderEntity` with constraints from data model: `id` "UUID, required, unique", `status` "enum, required: CREATED, PROCESSING, COMPLETED, CANCELED", `createdAt` "timestamp, required", `updatedAt` "timestamp, required", `canceledAt` "timestamp, optional", `currency` "string, required, ISO-4217", `totalAmount` "decimal, required, >= 0", `idempotencyKey` "string, required for create, unique with request fingerprint" in `src/main/java/com/example/shopping/orders/infra/OrderEntity.java`
- [ ] T024 [P] [US2] Create `OrderItemEntity` with constraints from data model: `id` "UUID, required, unique", `orderId` "UUID, required, FK -> Order.id", `productId` "UUID, required, FK -> Product.id", `quantity` "integer, required, >= 1", `unitPriceAmount` "decimal, required, positive", `lineAmount` "decimal, required, positive" and validation rule "lineAmount = unitPriceAmount * quantity" in `src/main/java/com/example/shopping/orders/infra/OrderItemEntity.java`
- [ ] T025 [P] [US2] Create `OrderIdempotencyEntity` with constraints from data model: `idempotencyKey` "string, required", `requestHash` "string, required", `orderId` "UUID, required", `createdAt` "timestamp, required" and validation rule "Reused idempotency key with different request hash returns conflict/error" in `src/main/java/com/example/shopping/orders/infra/OrderIdempotencyEntity.java`
- [ ] T026 [P] [US2] Implement repositories for order, order item, and idempotency records in `src/main/java/com/example/shopping/orders/infra/OrderRepository.java`, `src/main/java/com/example/shopping/orders/infra/OrderItemRepository.java`, and `src/main/java/com/example/shopping/orders/infra/OrderIdempotencyRepository.java`
- [ ] T027 [P] [US2] Create create-order request/response DTO records with Jakarta Validation annotations (including items min size and quantity >= 1) in `src/main/java/com/example/shopping/orders/api/CreateOrderRequest.java` and `src/main/java/com/example/shopping/orders/api/OrderResponse.java`
- [ ] T028 [US2] Implement order creation service with idempotency key handling and request fingerprint checks in `src/main/java/com/example/shopping/orders/domain/OrderCreationService.java`
- [ ] T029 [US2] Implement `POST /api/v1/orders` endpoint, `Idempotency-Key` header enforcement, and response mapping in `src/main/java/com/example/shopping/orders/api/OrdersController.java`

**Checkpoint**: User Stories 1 and 2 are both independently functional.

---

## Phase 5: User Story 3 - Track and Cancel an Order (Priority: P3)

**Goal**: Provide `GET /api/v1/orders/{id}` and `DELETE /api/v1/orders/{id}` with soft-cancel, repeated-delete no-op, and non-cancellable-state conflict behavior.

**Independent Test**: Retrieve existing order, cancel it, retrieve as `CANCELED`, repeat cancel as success no-op, and receive conflict for non-cancellable state.

### Tests for User Story 3

- [ ] T030 [P] [US3] Add contract tests for `GET /api/v1/orders/{id}` and `DELETE /api/v1/orders/{id}` success/not-found/conflict/error responses in `src/test/java/com/example/shopping/contract/OrdersLifecycleContractTest.java`
- [ ] T031 [P] [US3] Add integration tests for soft-cancel lifecycle, repeated delete no-op, and non-cancellable conflict in `src/test/java/com/example/shopping/integration/OrdersLifecycleIntegrationTest.java`
- [ ] T032 [P] [US3] Add unit tests for order lifecycle transition rules in `src/test/java/com/example/shopping/unit/orders/OrderLifecycleServiceTest.java`

### Implementation for User Story 3

- [ ] T033 [US3] Implement order query service for retrieval by UUID with standardized not-found behavior in `src/main/java/com/example/shopping/orders/domain/OrderQueryService.java`
- [ ] T034 [US3] Implement order cancellation service for transitions `CREATED -> CANCELED`, repeated `CANCELED -> CANCELED` no-op, and conflict on non-cancellable states in `src/main/java/com/example/shopping/orders/domain/OrderCancellationService.java`
- [ ] T035 [US3] Extend orders controller with `GET /api/v1/orders/{id}` and `DELETE /api/v1/orders/{id}` endpoints in `src/main/java/com/example/shopping/orders/api/OrdersController.java`
- [ ] T036 [US3] Add path variable UUID validation and conflict/not-found exception mapping for order lifecycle endpoints in `src/main/java/com/example/shopping/orders/api/OrdersController.java`

**Checkpoint**: All user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize cross-story quality and delivery readiness.

- [ ] T037 [P] Update and align OpenAPI examples for success/error/idempotency flows in `specs/001-expose-shopping-apis/contracts/openapi.yaml`
- [ ] T038 [P] Add production-safe logging and metrics for products and order lifecycle operations in `src/main/java/com/example/shopping/shared/config/ObservabilityConfig.java`
- [ ] T039 [P] Add Testcontainers PostgreSQL base test support for reuse across integration suites in `src/test/java/com/example/shopping/integration/support/PostgresContainerSupport.java`
- [ ] T040 [P] Update quickstart runbook with implemented commands and expected outcomes in `specs/001-expose-shopping-apis/quickstart.md`
- [ ] T041 Verify full test suite and Maven lifecycle execution command documentation in `README.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies; starts immediately.
- **Phase 2 (Foundational)**: Depends on Phase 1; blocks all user stories.
- **Phase 3 (US1)**: Depends on Phase 2; delivers MVP.
- **Phase 4 (US2)**: Depends on Phase 2 and reuses US1 product access paths.
- **Phase 5 (US3)**: Depends on Phase 4 order domain lifecycle.
- **Phase 6 (Polish)**: Depends on completion of targeted user stories.

### User Story Dependencies

- **US1 (P1)**: Independent after Foundational phase completion.
- **US2 (P2)**: Independent from UI perspective after Foundational, but uses product/order persistence from shared foundation.
- **US3 (P3)**: Depends on US2 order creation domain objects and services.

### Within Each User Story

- Tests first (write and fail before implementation).
- Entities/DTOs before services.
- Services before controllers.
- Controller wiring before end-to-end verification.

### Parallel Opportunities

- Setup tasks marked `[P]` can run concurrently (`T003`, `T004`, `T005`, `T006`).
- Foundational tasks marked `[P]` can run concurrently (`T008`, `T009`, `T010`, `T011`).
- US1 model/repository/DTO tasks can run concurrently (`T015`, `T016`, `T017`).
- US2 entity/repository/DTO tasks can run concurrently (`T023`, `T024`, `T025`, `T026`, `T027`).
- US3 test tasks can run concurrently (`T030`, `T031`, `T032`).

---

## Parallel Example: User Story 2

```bash
# Parallel test authoring:
Task: "T020 [US2] contract tests in src/test/java/com/example/shopping/contract/OrdersCreateContractTest.java"
Task: "T021 [US2] integration tests in src/test/java/com/example/shopping/integration/OrdersCreateIntegrationTest.java"
Task: "T022 [US2] unit tests in src/test/java/com/example/shopping/unit/orders/OrderCreationServiceTest.java"

# Parallel domain scaffolding:
Task: "T023 [US2] OrderEntity in src/main/java/com/example/shopping/orders/infra/OrderEntity.java"
Task: "T024 [US2] OrderItemEntity in src/main/java/com/example/shopping/orders/infra/OrderItemEntity.java"
Task: "T025 [US2] OrderIdempotencyEntity in src/main/java/com/example/shopping/orders/infra/OrderIdempotencyEntity.java"
Task: "T027 [US2] DTO records in src/main/java/com/example/shopping/orders/api/CreateOrderRequest.java and src/main/java/com/example/shopping/orders/api/OrderResponse.java"
```

---

## Implementation Strategy

### MVP First (US1 Only)

1. Complete Phase 1 (Setup).
2. Complete Phase 2 (Foundational).
3. Complete Phase 3 (US1).
4. Validate `GET /api/v1/products` against contract/integration tests.

### Incremental Delivery

1. Deliver US1 (catalog browsing).
2. Deliver US2 (order creation + idempotency).
3. Deliver US3 (order retrieval + cancellation lifecycle).
4. Execute Phase 6 polish tasks and full-suite validation.

### Parallel Team Strategy

1. Team completes Phase 1 and Phase 2 together.
2. One engineer drives US1, second drives US2 scaffolding, third prepares US3 tests.
3. Merge stories in priority order with contract compatibility checks.

---

## Notes

- `[P]` tasks modify independent files and can be parallelized safely.
- `[USx]` labels maintain traceability from `spec.md` to implementation tasks.
- Keep configuration externalized; do not introduce hard-coded credentials or secrets.
- Keep JSON serialization on Jackson 3 only (no Jackson 2 dependencies).

