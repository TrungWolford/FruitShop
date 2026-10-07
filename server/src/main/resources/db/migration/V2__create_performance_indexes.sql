-- =============================================================================
-- V2__create_performance_indexes.sql
-- Tao index cho cac column duoc su dung nhieu nhat trong read queries.
--
-- Schema verified tu: docker exec postgres psql -U admin -d fruitshop -c "\d <table>"
--
-- TABLE: products      -> product_id, product_name, price, stock, status, created_at, updated_at
-- TABLE: accounts      -> account_id, account_name, account_phone, password, status
-- TABLE: orders        -> order_id, accountid, paymentid, status, created_at, total_amount
-- TABLE: carts         -> cart_id, accountid, status, created_at  [UNIQUE(accountid) da co]
-- TABLE: cartitems     -> cart_item_id, cartid, productid, quantity
-- TABLE: ratings       -> rating_id, account_id, product_id, order_item_id, comment, rating_star, status, created_at, updated_at
-- TABLE: payments      -> payment_id, amount, payment_date, payment_method, payment_status, transaction_id
-- TABLE: shippings     -> shipping_id, accountid, orderid, status, ...  [UNIQUE(orderid) da co]
-- TABLE: refunds       -> refund_id, order_id, order_item_id, refund_status, requested_at, ...
-- TABLE: chat_messages -> message_id, session_id, sender_id, sender_role, status, deleted, created_at, ...
-- TABLE: chat_sessions -> session_id, account_id, status, ...
-- TABLE: product_category -> productid, categoryid
--
-- RULE:
--   KHONG tao index trung voi PRIMARY KEY hay UNIQUE constraint da co san.
--   Chi tao non-unique index hoac unique index cho cot CHUA co constraint.
-- =============================================================================

-- Bat trigram extension truoc khi tao GIN index
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =============================================================================
-- TABLE: products
-- Columns: product_id(PK), product_name, price, stock, status
-- =============================================================================

-- price: range filter BETWEEN
CREATE INDEX IF NOT EXISTS idx_products_price
    ON products (price);

-- stock: ORDER BY stock ASC LIMIT N
CREATE INDEX IF NOT EXISTS idx_products_stock
    ON products (stock ASC);

-- Composite (status, price): filter status + range price
CREATE INDEX IF NOT EXISTS idx_products_status_price
    ON products (status, price);

-- product_name: LIKE '%%keyword%%' -- GIN trigram
CREATE INDEX IF NOT EXISTS idx_products_productname_trgm
    ON products USING GIN (product_name gin_trgm_ops);

-- =============================================================================
-- TABLE: accounts
-- Columns: account_id(PK), account_name, account_phone, status
-- =============================================================================

-- account_phone: login/lookup -- tao UNIQUE index (chua co constraint trong schema)
CREATE UNIQUE INDEX IF NOT EXISTS idx_accounts_phone_unique
    ON accounts (account_phone);

-- account_name: LIKE search -- GIN trigram
CREATE INDEX IF NOT EXISTS idx_accounts_accountname_trgm
    ON accounts USING GIN (account_name gin_trgm_ops);

-- =============================================================================
-- TABLE: orders
-- Columns: order_id(PK), accountid(FK), status, created_at, total_amount
-- =============================================================================

-- accountid: lookup orders cua user
CREATE INDEX IF NOT EXISTS idx_orders_accountid
    ON orders (accountid);

-- Composite (accountid, status): orders cua user X o trang thai Y
CREATE INDEX IF NOT EXISTS idx_orders_accountid_status
    ON orders (accountid, status);

-- =============================================================================
-- TABLE: carts
-- Columns: cart_id(PK), accountid(FK), status, created_at
-- NOTE: UNIQUE(accountid) da duoc Hibernate tao san -> SKIP tao them
-- =============================================================================

-- Khong can tao them index cho accountid -- da co UNIQUE constraint "ukamf7m1pumpif9plvktcedln0v"

-- =============================================================================
-- TABLE: cartitems
-- Columns: cart_item_id(PK), cartid(FK), productid(FK), quantity
-- =============================================================================

-- cartid: lay tat ca items trong cart
CREATE INDEX IF NOT EXISTS idx_cartitems_cartid
    ON cartitems (cartid);

-- Composite (cartid, productid): check trung lap item truoc khi add
CREATE INDEX IF NOT EXISTS idx_cartitems_cart_product
    ON cartitems (cartid, productid);

-- =============================================================================
-- TABLE: ratings
-- Columns: rating_id(PK), account_id(FK), product_id(FK), order_item_id(FK), status, created_at
-- =============================================================================

-- product_id: load ratings cua san pham
CREATE INDEX IF NOT EXISTS idx_ratings_product_id
    ON ratings (product_id);

-- account_id: lookup ratings cua user
CREATE INDEX IF NOT EXISTS idx_ratings_account_id
    ON ratings (account_id);

-- order_item_id: 1 orderItem -> 1 rating (UNIQUE)
CREATE UNIQUE INDEX IF NOT EXISTS idx_ratings_orderitem_unique
    ON ratings (order_item_id);

-- =============================================================================
-- TABLE: payments
-- Columns: payment_id(PK), amount, payment_date, payment_method, payment_status, transaction_id
-- NOTE: Verified tu DB: Hibernate convert @Column(name="transactionId") -> transaction_id
-- =============================================================================

-- transaction_id: exact lookup tu cong thanh toan -- UNIQUE
CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_transactionid_unique
    ON payments (transaction_id);

-- Composite (payment_method, payment_status): combined filter
CREATE INDEX IF NOT EXISTS idx_payments_method_status
    ON payments (payment_method, payment_status);

-- =============================================================================
-- TABLE: shippings
-- Columns: shipping_id(PK), accountid(FK), orderid(FK), status, ...
-- NOTE: UNIQUE(orderid) da duoc Hibernate tao san -> SKIP tao them
-- =============================================================================

-- accountid: lookup lich su giao hang cua user
CREATE INDEX IF NOT EXISTS idx_shippings_accountid
    ON shippings (accountid);

-- Khong can tao them index cho orderid -- da co UNIQUE constraint "uknp1ql93uliwhoglgsnhan8gct"

-- =============================================================================
-- TABLE: refunds
-- Columns: refund_id(PK), order_id(FK), order_item_id(FK), refund_status, requested_at
-- =============================================================================

-- order_id: lookup refunds cua order
CREATE INDEX IF NOT EXISTS idx_refunds_order_id
    ON refunds (order_id);

-- order_item_id: lookup refund theo item
CREATE INDEX IF NOT EXISTS idx_refunds_orderitem_id
    ON refunds (order_item_id);

-- =============================================================================
-- TABLE: chat_messages  (HIGH FREQUENCY)
-- Columns: message_id(PK), session_id(FK), sender_id(FK), sender_role, status, deleted, created_at
-- =============================================================================

-- Composite (session_id, created_at): load messages theo session + sort theo thoi gian
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_createdat
    ON chat_messages (session_id, created_at ASC);

-- Composite (session_id, deleted, created_at): filter soft-delete + sort
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_deleted_createdat
    ON chat_messages (session_id, deleted, created_at ASC);

-- Composite (session_id, sender_role, status): dem unread messages cua CUSTOMER
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_role_status
    ON chat_messages (session_id, sender_role, status);

-- =============================================================================
-- TABLE: chat_sessions
-- Columns: session_id(PK), account_id(FK), status, ...
-- =============================================================================

-- account_id: lookup sessions cua user
CREATE INDEX IF NOT EXISTS idx_chat_sessions_account_id
    ON chat_sessions (account_id);

-- =============================================================================
-- TABLE: product_category  (join table ManyToMany)
-- Columns: productid(FK), categoryid(FK)
-- NOTE: UNIQUE(productid, categoryid) da duoc Hibernate tao san
-- =============================================================================

-- categoryid: lookup products thuoc category
CREATE INDEX IF NOT EXISTS idx_product_category_categoryid
    ON product_category (categoryid);
