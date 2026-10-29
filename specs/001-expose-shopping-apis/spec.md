# Feature Specification: Expose Shopping APIs

**Feature Branch**: `001-expose-shopping-apis`

**Created**: 2026-10-27

**Status**: Draft

**Input**: User description: "Expose the follwoing APIs:
GET /api/v1/products
POST /api/v1/products
POST /api/v1/orders
GET /api/v1/orders/{id}
DELETE /api/v1/orders/{id}"

## Clarifications

### Session 2026-10-27

- Q: After `DELETE /api/v1/orders/{id}`, should the order remain retrievable with a canceled status or be treated as deleted and no longer retrievable? → A: Soft cancel: order remains retrievable and shows a canceled status.
- Q: When a client retries `POST /api/v1/orders` due to network timeout, how should duplicate submissions be handled? → A: Idempotency key required; repeated requests with same key return the original created order.
- Q: If `DELETE /api/v1/orders/{id}` is called for an order that is already canceled, should the API treat it as a successful no-op or return a conflict? → A: Return success (no-op) when order is already canceled.
- Q: If `DELETE /api/v1/orders/{id}` is called for an order that has progressed to a non-cancellable state, what response should the API return? → A: Return conflict indicating the order cannot be canceled in its current state.

### Session 2026-10-28

- Q: How should `POST /api/v1/orders` respond when the `Idempotency-Key` header is missing? → A: Reject request with standardized validation error indicating `Idempotency-Key` is required.

### Session 2026-10-29

- Q: Which product field should be the unique catalog identifier used to reject duplicates in `POST /api/v1/products`? → A: Use `sku` as the unique identifier.
- Q: How should the API respond when `GET /api/v1/orders/{id}` or `DELETE /api/v1/orders/{id}` receives a malformed order ID format? → A: Return standardized validation error for malformed ID.
- Q: How should `POST /api/v1/orders` behave when catalog data is temporarily unavailable while validating requested products? → A: Return standardized service unavailable error (retryable).
- Q: Which product fields are required in `POST /api/v1/products` for a request to be considered valid? → A: Require `sku`, `name`, and `price`.
- Q: Which order statuses should be treated as non-cancellable for `DELETE /api/v1/orders/{id}`? → A: `SHIPPED` and `COMPLETED` are non-cancellable.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Browse Product Catalog (Priority: P1)

As a shopper, I can view the current product list so I can decide what to order.

**Why this priority**: Product visibility is the entry point for order creation and enables the
minimum valuable experience.

**Independent Test**: Can be fully tested by requesting the product listing endpoint and verifying
that the response contains available products with required display information.

**Acceptance Scenarios**:

1. **Given** products exist in the catalog, **When** the client requests the products list,
   **Then** the system returns a successful response containing those products.
2. **Given** no products are available, **When** the client requests the products list,
   **Then** the system returns a successful response with an empty collection.

---

### User Story 2 - Add a Product to Catalog (Priority: P2)

As a catalog manager, I can add a product so it becomes available in the product catalog.

**Why this priority**: Product onboarding is required to keep the catalog current and directly
supports product browsing and order placement.

**Independent Test**: Can be fully tested by submitting a valid product creation request and
verifying the product is created and returned in a successful response.

**Acceptance Scenarios**:

1. **Given** a valid product payload containing `sku`, `name`, and `price`,
   **When** the client submits `POST /api/v1/products`,
   **Then** the system creates the product and returns a success response with the created product
   details.
2. **Given** an invalid product payload, **When** the client submits `POST /api/v1/products`,
   **Then** the system rejects the request and returns a standardized validation error response.
3. **Given** a product already exists with the same `sku`,
   **When** the client submits `POST /api/v1/products` with that `sku`,
   **Then** the system returns a standardized conflict response and does not create a duplicate
   product.

---

### User Story 3 - Place an Order (Priority: P3)

As a shopper, I can submit an order so my selected products can be processed.

**Why this priority**: Order submission delivers direct business value, but depends on users being
able to discover products first.

**Independent Test**: Can be fully tested by submitting a valid order request and verifying the
system creates a new order with a unique identifier.

**Acceptance Scenarios**:

1. **Given** a valid order request with available products, **When** the client submits a new
   order, **Then** the system creates the order and returns a success response with the order
   identifier.
2. **Given** an invalid order request, **When** the client submits the order, **Then** the system
   rejects the request and returns a standardized validation error response.
3. **Given** a previously successful order request and the same idempotency key,
   **When** the client retries `POST /api/v1/orders`, **Then** the system returns the original
   order result and does not create a duplicate order.
4. **Given** a request is submitted without `Idempotency-Key`, **When** the client calls
   `POST /api/v1/orders`, **Then** the system rejects the request with a standardized validation
   error indicating the missing required header.
5. **Given** product catalog data is temporarily unavailable during order validation,
   **When** the client submits `POST /api/v1/orders`, **Then** the system returns a standardized
   service unavailable error response indicating the request is retryable.

---

### User Story 4 - Track and Cancel an Order (Priority: P4)

As a shopper, I can retrieve an order by ID and cancel it when needed.

**Why this priority**: Retrieval and cancellation complete the order lifecycle and improve user
control after checkout.

**Independent Test**: Can be fully tested by fetching an existing order by ID and then deleting it,
verifying the order remains retrievable and is marked as canceled.

**Acceptance Scenarios**:

1. **Given** an existing order ID, **When** the client requests that order,
   **Then** the system returns the matching order details.
2. **Given** a non-existing order ID, **When** the client requests that order,
   **Then** the system returns a standardized not-found error response.
3. **Given** an existing cancellable order ID, **When** the client requests order deletion,
   **Then** the system marks the order as canceled and confirms cancellation.
4. **Given** an order has already been canceled, **When** the client requests that order,
   **Then** the system returns the order details with a canceled status.
5. **Given** an order is already canceled, **When** the client requests deletion again,
   **Then** the system returns a successful no-op response and does not change order data.
6. **Given** an order is in a non-cancellable state, **When** the client requests deletion,
   **Then** the system returns a standardized conflict response indicating cancellation is not
   allowed for the current order state (`SHIPPED` or `COMPLETED`).
7. **Given** an order ID format is malformed, **When** the client requests retrieval or deletion,
   **Then** the system returns a standardized validation error response.

---

### Edge Cases

- `GET /api/v1/orders/{id}` and `DELETE /api/v1/orders/{id}` with malformed ID format return a
  standardized validation error response.
- `POST /api/v1/products` submitted without `sku`, `name`, or `price` returns a standardized
  validation error response.
- What happens when `POST /api/v1/products` is submitted with a duplicate `sku`?
- `POST /api/v1/orders` submitted without `Idempotency-Key` returns a standardized validation
  error indicating the required header is missing.
- `DELETE /api/v1/orders/{id}` for an already canceled order returns a successful no-op response.
- `DELETE /api/v1/orders/{id}` returns a standardized conflict response when the order status is
  `SHIPPED` or `COMPLETED`.
- `POST /api/v1/orders` returns a standardized service unavailable error response when product data
  is temporarily unavailable during order creation.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST expose `GET /api/v1/products` to return the current product collection.
- **FR-002**: System MUST expose `POST /api/v1/products` to create a new product from a valid
  client request and persist it for subsequent catalog reads.
- **FR-003**: System MUST expose `POST /api/v1/orders` to create a new order from a valid client
  request.
- **FR-004**: System MUST expose `GET /api/v1/orders/{id}` to return details for the specified
  order when it exists.
- **FR-005**: System MUST expose `DELETE /api/v1/orders/{id}` to cancel or remove the specified
  order by transitioning it to a canceled status.
- **FR-006**: System MUST return standardized error responses for validation failures,
  resource-not-found cases, and other client-visible request errors.
- **FR-007**: System MUST version all listed endpoints under `/api/v1` and preserve backward
  compatibility for this version after release.
- **FR-008**: System MUST ensure that successful order creation returns a unique order identifier
  that can be used for subsequent retrieval and deletion operations.
- **FR-009**: System MUST ensure that canceled orders remain retrievable through
  `GET /api/v1/orders/{id}` and are clearly identified as canceled.
- **FR-010**: System MUST require an idempotency key for `POST /api/v1/orders` and MUST return the
  original successful result for retries that reuse the same key and equivalent request payload.
- **FR-011**: System MUST treat repeated `DELETE /api/v1/orders/{id}` on an already canceled order
  as an idempotent successful no-op.
- **FR-012**: System MUST return a standardized conflict response when
  `DELETE /api/v1/orders/{id}` is requested for an order in a non-cancellable state (`SHIPPED` or
  `COMPLETED`).
- **FR-013**: System MUST reject `POST /api/v1/orders` requests that omit `Idempotency-Key` with
  a standardized validation error response identifying the missing required header.
- **FR-014**: System MUST return a standardized conflict response when `POST /api/v1/products`
  attempts to create a product that duplicates an existing `sku`.
- **FR-015**: System MUST return a standardized validation error response when
  `GET /api/v1/orders/{id}` or `DELETE /api/v1/orders/{id}` receives a malformed order ID format.
- **FR-016**: System MUST return a standardized service unavailable error response for
  `POST /api/v1/orders` when required product catalog data is temporarily unavailable, and the
  response MUST indicate the request can be retried.
- **FR-017**: System MUST require `sku`, `name`, and `price` in `POST /api/v1/products` and MUST
  return a standardized validation error response when any required field is missing.

### Key Entities *(include if feature involves data)*

- **Product**: A sellable catalog item with required `sku`, `name`, and `price`, where `sku` is
  unique and shopper-visible attributes support listing.
- **Order**: A shopper purchase request identified by a unique ID, including lifecycle status and
  references to selected products.
- **Order Item**: A line-level selection linking an order to a product and quantity.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of API requests to the five defined routes return either a valid success
  response or a standardized error response.
- **SC-002**: At least 95% of valid order creation attempts complete successfully on first
  submission during acceptance testing.
- **SC-003**: At least 95% of users in validation testing can complete the flow of viewing
  products, placing an order, and retrieving that order without assistance.
- **SC-004**: Requests for non-existent orders are correctly identified and reported in 100% of
  test cases with the standardized not-found response format.
- **SC-005**: At least 95% of valid product creation attempts complete successfully on first
  submission during acceptance testing.

## Assumptions

- API consumers are authenticated/authorized by an existing mechanism outside this feature scope.
- Product catalog data is managed by existing catalog capabilities and is available for read access.
- Order cancellation uses soft cancel semantics and does not hard-delete order records.
- This feature scope is limited to the five listed endpoints and excludes additional order
  management operations.

