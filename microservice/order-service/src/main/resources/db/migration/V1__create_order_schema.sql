CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    payment_id VARCHAR(36),
    total_amount BIGINT NOT NULL,
    status INTEGER NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price BIGINT NOT NULL,
    status VARCHAR(50),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS shippings (
    shipping_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) UNIQUE NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    receiver_name VARCHAR(255),
    receiver_phone VARCHAR(20),
    receiver_address VARCHAR(500),
    city VARCHAR(100),
    shipper_name VARCHAR(255),
    shipping_fee BIGINT NOT NULL DEFAULT 0,
    shipped_at TIMESTAMP,
    status INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_shippings_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS refunds (
    refund_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    order_item_id VARCHAR(36),
    reason VARCHAR(2000),
    refund_status VARCHAR(50),
    requested_at TIMESTAMP,
    processed_at TIMESTAMP,
    refund_amount BIGINT NOT NULL DEFAULT 0,
    image_urls TEXT,
    original_payment_id VARCHAR(36),
    CONSTRAINT fk_refunds_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_refunds_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_orders_account_id ON orders(account_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_refunds_order_id ON refunds(order_id);
