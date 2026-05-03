CREATE TABLE IF NOT EXISTS categories (
    category_id VARCHAR(36) PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL UNIQUE,
    status INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS products (
    product_id VARCHAR(36) PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    price BIGINT NOT NULL,
    stock BIGINT NOT NULL,
    description VARCHAR(3600),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    status INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS product_category (
    product_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_product_category_product
        FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    CONSTRAINT fk_product_category_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_images (
    id BIGSERIAL PRIMARY KEY,
    image_url VARCHAR(1000),
    image_order INTEGER DEFAULT 0,
    is_main BOOLEAN DEFAULT FALSE,
    product_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);
