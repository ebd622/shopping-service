CREATE TABLE products (
  id UUID PRIMARY KEY,
  sku VARCHAR(100) NOT NULL UNIQUE,
  name VARCHAR(200) NOT NULL,
  description VARCHAR(2000),
  price_amount NUMERIC(12,2) NOT NULL CHECK (price_amount > 0),
  price_currency VARCHAR(3) NOT NULL,
  available BOOLEAN NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE orders (
  id UUID PRIMARY KEY,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  canceled_at TIMESTAMPTZ,
  currency VARCHAR(3) NOT NULL,
  total_amount NUMERIC(12,2) NOT NULL CHECK (total_amount >= 0)
);

CREATE TABLE order_items (
  id UUID PRIMARY KEY,
  order_id UUID NOT NULL REFERENCES orders(id),
  product_id UUID NOT NULL REFERENCES products(id),
  quantity INTEGER NOT NULL CHECK (quantity >= 1),
  unit_price_amount NUMERIC(12,2) NOT NULL CHECK (unit_price_amount > 0),
  line_amount NUMERIC(12,2) NOT NULL CHECK (line_amount > 0)
);

CREATE TABLE order_idempotency (
  idempotency_key VARCHAR(255) PRIMARY KEY,
  request_hash VARCHAR(64) NOT NULL,
  order_id UUID NOT NULL REFERENCES orders(id),
  created_at TIMESTAMPTZ NOT NULL
);

