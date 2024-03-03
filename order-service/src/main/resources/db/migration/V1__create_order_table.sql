CREATE TABLE IF NOT EXISTS "order" (
    id UUID PRIMARY KEY,
    instrument TEXT NOT NULL,
    order_state TEXT NOT NULL,
    order_type TEXT NOT NULL,
    price NUMERIC,
    target_quantity NUMERIC NOT NULL,
    fulfilled_quantity NUMERIC NOT NULL,
    trader_id BIGINT NOT NULL,
    matching_requests BYTEA
);