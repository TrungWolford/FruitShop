CREATE TABLE IF NOT EXISTS carts (
    cart_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL UNIQUE,
    created_at TIMESTAMP,
    status INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id VARCHAR(36) PRIMARY KEY,
    cart_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id ON cart_items(product_id);
