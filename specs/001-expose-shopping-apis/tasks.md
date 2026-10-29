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

**Purpose**: Initialize project baseline and tooling.

- [X] T001 Pin Spring Boot `4.1.2`, Java 21, Jackson 3-only policy, PostgreSQL, OpenAPI, Actuator/Micrometer, JUnit 5, Mockito, and Testcontainers dependencies in `pom.xml`
- [X] T002 Configure container build and runtime assets in `Dockerfile`, `docker-compose.yml`, and `.dockerignore`
- [X] T003 [P] Externalize environment configuration placeholders and profile defaults in `.env.example` and `src/main/resources/application.yml`
- [X] T004 [P] Ensure application bootstrap class and package root structure exist in `src/main/java/com/example/shopping/ShoppingServiceApplication.java`
- [X] T005 [P] Create feature package skeletons for `products`, `orders`, and shared modules in `src/main/java/com/example/shopping/products/`, `src/main/java/com/example/shopping/orders/`, `src/main/java/com/example/shopping/shared/error/`, and `src/main/java/com/example/shopping/shared/config/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Implement core infrastructure that blocks all user stories.

**⚠️ CRITICAL**: No user story implementation should start before this phase is complete.

- [X] T006 Create Flyway schema for `products`, `orders`, `order_items`, and `order_idempotency` in `src/main/resources/db/migration/V1__init_schema.sql`
- [X] T007 [P] Implement standardized API error envelope (`code`, `message`, `details`, `timestamp`, `path`, `traceId`) in `src/main/java/com/example/shopping/shared/error/ApiErrorResponse.java`
- [X] T008 [P] Implement global exception mapping for validation/not-found/conflict/internal errors in `src/main/java/com/example/shopping/shared/error/GlobalExceptionHandler.java`
- [X] T009 [P] Configure OpenAPI v1 metadata and shared error schemas in `src/main/java/com/example/shopping/shared/config/OpenApiConfig.java`
- [X] T010 [P] Configure Actuator/Micrometer baseline metrics and tags in `src/main/java/com/example/shopping/shared/config/ObservabilityConfig.java`
- [X] T011 [P] Configure persistence timestamps/auditing and PostgreSQL settings in `src/main/java/com/example/shopping/shared/config/PersistenceConfig.java`
- [X] T012 Add reusable integration test base with PostgreSQL Testcontainers in `src/test/java/com/example/shopping/integration/support/PostgresContainerSupport.java`

**Checkpoint**: Foundation complete; user story phases can proceed.

---

## Phase 3: User Story 1 - Browse Product Catalog (Priority: P1) 🎯 MVP

**Goal**: Provide `GET /api/v1/products` with valid catalog payloads and standardized errors.

**Independent Test**: Calling `GET /api/v1/products` returns `200` and a valid product array (possibly empty), matching `contracts/openapi.yaml`.

### Tests for User Story 1

- [X] T013 [P] [US1] Add contract test for `GET /api/v1/products` response schema in `src/test/java/com/example/shopping/contract/ProductsContractTest.java`
- [X] T014 [P] [US1] Add integration tests for non-empty and empty catalog responses in `src/test/java/com/example/shopping/integration/ProductsIntegrationTest.java`

### Implementation for User Story 1

- [X] T015 [P] [US1] Create `ProductEntity` with constraints: `id` "UUID, required, unique", `sku` "string, required, unique", `name` "string, required, 1..200", `description` "string, optional, <=2000", `priceAmount` "decimal, required, positive", `priceCurrency` "string, required, ISO-4217", `available` "boolean, required", `updatedAt` "timestamp, required" in `src/main/java/com/example/shopping/products/infra/ProductEntity.java`
- [X] T016 [P] [US1] Create product repository in `src/main/java/com/example/shopping/products/infra/ProductRepository.java`
- [X] T017 [P] [US1] Create API DTO record for product output in `src/main/java/com/example/shopping/products/api/ProductResponse.java`
- [X] T018 [US1] Implement product query service in `src/main/java/com/example/shopping/products/domain/ProductQueryService.java`
- [X] T019 [US1] Implement `GET /api/v1/products` endpoint and mapping in `src/main/java/com/example/shopping/products/api/ProductsController.java`

**Checkpoint**: User Story 1 is independently functional and testable.

---

## Phase 4: User Story 2 - Place an Order (Priority: P2)

**Goal**: Provide `POST /api/v1/orders` with validation, required `Idempotency-Key`, idempotent retry, and deterministic conflict handling.

**Independent Test**: Valid create returns created order; same key + equivalent payload returns original result; missing key returns standardized validation error.

### Tests for User Story 2

- [X] T020 [P] [US2] Add contract tests for `POST /api/v1/orders` covering `201`, validation errors (including missing `Idempotency-Key`), and `409` conflict in `src/test/java/com/example/shopping/contract/OrdersCreateContractTest.java`
- [X] T021 [P] [US2] Add integration tests for create, idempotent retry, and missing header rejection in `src/test/java/com/example/shopping/integration/OrdersCreateIntegrationTest.java`
- [X] T022 [P] [US2] Add unit tests for idempotency and request-hash conflict logic in `src/test/java/com/example/shopping/unit/orders/OrderCreationServiceTest.java`

### Implementation for User Story 2

- [X] T023 [P] [US2] Create `OrderEntity` with constraints: `id` "UUID, required, unique", `status` "enum, required: CREATED, PROCESSING, COMPLETED, CANCELED", `createdAt` "timestamp, required", `updatedAt` "timestamp, required", `canceledAt` "timestamp, optional", `currency` "string, required, ISO-4217", `totalAmount` "decimal, required, >= 0", `idempotencyKey` "string, required for create, unique with request fingerprint" in `src/main/java/com/example/shopping/orders/infra/OrderEntity.java`
- [X] T024 [P] [US2] Create `OrderItemEntity` with constraints: `id` "UUID, required, unique", `orderId` "UUID, required, FK -> Order.id", `productId` "UUID, required, FK -> Product.id", `quantity` "integer, required, >= 1", `unitPriceAmount` "decimal, required, positive", `lineAmount` "decimal, required, positive" and rule "lineAmount = unitPriceAmount * quantity" in `src/main/java/com/example/shopping/orders/infra/OrderItemEntity.java`
- [X] T025 [P] [US2] Create `OrderIdempotencyEntity` with constraints: `idempotencyKey` "string, required", `requestHash` "string, required", `orderId` "UUID, required", `createdAt` "timestamp, required" and rule "Reused idempotency key with different request hash returns conflict/error" in `src/main/java/com/example/shopping/orders/infra/OrderIdempotencyEntity.java`
- [X] T026 [P] [US2] Implement order repositories in `src/main/java/com/example/shopping/orders/infra/OrderRepository.java`, `src/main/java/com/example/shopping/orders/infra/OrderItemRepository.java`, and `src/main/java/com/example/shopping/orders/infra/OrderIdempotencyRepository.java`
- [X] T027 [P] [US2] Implement create-order DTO records with Jakarta Validation (`items` min 1, `quantity >= 1`) in `src/main/java/com/example/shopping/orders/api/CreateOrderRequest.java` and `src/main/java/com/example/shopping/orders/api/OrderResponse.java`
- [X] T028 [US2] Implement order creation service with idempotency key and request fingerprint logic in `src/main/java/com/example/shopping/orders/domain/OrderCreationService.java`
- [X] T029 [US2] Implement `POST /api/v1/orders` endpoint with mandatory `Idempotency-Key` enforcement in `src/main/java/com/example/shopping/orders/api/OrdersController.java`

**Checkpoint**: User Stories 1 and 2 are independently functional.

---

## Phase 5: User Story 3 - Track and Cancel an Order (Priority: P3)

**Goal**: Provide `GET /api/v1/orders/{id}` and `DELETE /api/v1/orders/{id}` with soft-cancel retrieval, repeated-delete no-op, and non-cancellable conflict behavior.

**Independent Test**: Retrieve existing order, cancel to `CANCELED`, retrieve canceled order, repeat delete as success no-op, and receive `409` for non-cancellable state.

### Tests for User Story 3

- [X] T030 [P] [US3] Add contract tests for `GET/DELETE /api/v1/orders/{id}` covering success, not found, validation, and conflict responses in `src/test/java/com/example/shopping/contract/OrdersLifecycleContractTest.java`
- [X] T031 [P] [US3] Add integration tests for soft-cancel lifecycle, repeated delete no-op, and non-cancellable conflict in `src/test/java/com/example/shopping/integration/OrdersLifecycleIntegrationTest.java`
- [X] T032 [P] [US3] Add unit tests for cancellation state transitions and conflict rules in `src/test/java/com/example/shopping/unit/orders/OrderLifecycleServiceTest.java`

### Implementation for User Story 3

- [X] T033 [US3] Implement order query service with standardized not-found behavior in `src/main/java/com/example/shopping/orders/domain/OrderQueryService.java`
- [X] T034 [US3] Implement cancellation service transitions: `CREATED -> CANCELED`, `CANCELED -> CANCELED` no-op, and conflict on non-cancellable states in `src/main/java/com/example/shopping/orders/domain/OrderCancellationService.java`
- [X] T035 [US3] Implement `GET /api/v1/orders/{id}` and `DELETE /api/v1/orders/{id}` controller endpoints in `src/main/java/com/example/shopping/orders/api/OrdersController.java`
- [X] T036 [US3] Add UUID path validation and conflict/not-found exception wiring for lifecycle endpoints in `src/main/java/com/example/shopping/orders/api/OrdersController.java`

**Checkpoint**: All user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize cross-story quality, observability, and delivery readiness.

- [X] T037 [P] Align contract examples for success/error/idempotency/missing-header flows in `specs/001-expose-shopping-apis/contracts/openapi.yaml`
- [X] T038 [P] Add/verify operational metrics and structured logs for product and order flows in `src/main/java/com/example/shopping/shared/config/ObservabilityConfig.java`
- [X] T039 [P] Update validation/run guidance with finalized API behavior in `specs/001-expose-shopping-apis/quickstart.md`
- [X] T040 [P] Update implementation usage notes and build/test commands in `README.md`
- [X] T041 Verify full test execution (`unit`, `integration`, `contract`) and CI readiness settings in `pom.xml`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies; starts immediately.
- **Phase 2 (Foundational)**: Depends on Phase 1; blocks all user stories.
- **Phase 3 (US1)**: Depends on Phase 2; delivers MVP.
- **Phase 4 (US2)**: Depends on Phase 2 and reuses foundational persistence/error handling.
- **Phase 5 (US3)**: Depends on US2 order domain objects/services.
- **Phase 6 (Polish)**: Depends on completion of selected user stories.

### User Story Dependencies

- **US1 (P1)**: Independent after Foundational phase completion.
- **US2 (P2)**: Independent after Foundational; reuses shared persistence and validation layers.
- **US3 (P3)**: Depends on US2 order creation/lifecycle model.

### Within Each User Story

- Tests first (write and fail before implementation).
- Entities/DTOs before services.
- Services before controllers.
- Controller wiring before end-to-end verification.

### Parallel Opportunities

- Setup tasks marked `[P]` can run concurrently (`T003`, `T004`, `T005`).
- Foundational tasks marked `[P]` can run concurrently (`T007`, `T008`, `T009`, `T010`, `T011`).
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
4. Validate `GET /api/v1/products` against contract and integration tests.

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

