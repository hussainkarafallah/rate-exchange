CREATE TABLE orders (
    id UUID PRIMARY KEY,
    instrument VARCHAR(255) NOT NULL,
    order_state VARCHAR(255) NOT NULL,
    order_type VARCHAR(255) NOT NULL,
    trader_id BIGINT NOT NULL,
    fulfillments BYTEA
);
