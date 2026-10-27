# Data Model: Expose Shopping APIs

## Product

- Description: Catalog item exposed by `GET /api/v1/products`.
- Fields:
  - `id` (UUID, required, unique)
  - `sku` (string, required, unique)
  - `name` (string, required, 1..200)
  - `description` (string, optional, <=2000)
  - `priceAmount` (decimal, required, positive)
  - `priceCurrency` (string, required, ISO-4217)
  - `available` (boolean, required)
  - `updatedAt` (timestamp, required)
- Validation rules:
  - `sku`, `name`, `priceAmount`, `priceCurrency` required for active products.
  - `priceAmount` must be > 0.

## Order

- Description: Shopper order created via `POST /api/v1/orders` and managed via `GET/DELETE` by ID.
- Fields:
  - `id` (UUID, required, unique)
  - `status` (enum, required: `CREATED`, `PROCESSING`, `COMPLETED`, `CANCELED`)
  - `createdAt` (timestamp, required)
  - `updatedAt` (timestamp, required)
  - `canceledAt` (timestamp, optional)
  - `currency` (string, required, ISO-4217)
  - `totalAmount` (decimal, required, >= 0)
  - `idempotencyKey` (string, required for create, unique with request fingerprint)
- Validation rules:
  - Creation request must include at least one order item.
  - `status=CANCELED` requires `canceledAt`.
  - `idempotencyKey` must be present on create requests.

## OrderItem

- Description: Line item within an order.
- Fields:
  - `id` (UUID, required, unique)
  - `orderId` (UUID, required, FK -> Order.id)
  - `productId` (UUID, required, FK -> Product.id)
  - `quantity` (integer, required, >= 1)
  - `unitPriceAmount` (decimal, required, positive)
  - `lineAmount` (decimal, required, positive)
- Validation rules:
  - `quantity >= 1`
  - `lineAmount = unitPriceAmount * quantity` at creation time

## Idempotency Record

- Description: Request deduplication metadata for order creation retries.
- Fields:
  - `idempotencyKey` (string, required)
  - `requestHash` (string, required)
  - `orderId` (UUID, required)
  - `createdAt` (timestamp, required)
- Validation rules:
  - Reused idempotency key with different request hash returns conflict/error.

## Relationships

- Product 1..* OrderItem
- Order 1..* OrderItem
- Order 1..1 Idempotency Record (for create request identity)

## State Transitions

- `CREATED -> PROCESSING`
- `PROCESSING -> COMPLETED`
- `CREATED -> CANCELED`
- `PROCESSING -> CANCELED` (only if business rules allow)
- `COMPLETED` is non-cancellable
- `CANCELED -> CANCELED` via repeated DELETE (idempotent no-op)

## API-Driven Behavioral Constraints

- `GET /api/v1/orders/{id}` returns canceled orders with `status=CANCELED`.
- `DELETE /api/v1/orders/{id}` on already canceled order returns success no-op.
- `DELETE /api/v1/orders/{id}` on non-cancellable order returns conflict.

