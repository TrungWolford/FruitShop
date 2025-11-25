-- ===============================
-- SCRIPT SAMPLE DATA CHO FRUITSHOP (Fixed)
-- Chạy trên pgAdmin 4 với password đã hash
-- ===============================

-- 1. INSERT ROLES
INSERT INTO roles (role_id, role_name) VALUES
('admin-role-uuid-001', 'ADMIN'),
('customer-role-uuid-002', 'CUSTOMER'),
('staff-role-uuid-003', 'STAFF')
ON CONFLICT (role_id) DO NOTHING;

-- 2. INSERT CATEGORIES  
INSERT INTO categories (category_id, category_name, status) VALUES
('cat-fruits-uuid-001', 'Trái cây tươi', 1),
('cat-vegetables-uuid-002', 'Rau củ quả', 1), 
('cat-nuts-uuid-003', 'Hạt dinh dưỡng', 1)
ON CONFLICT (category_id) DO NOTHING;

-- 3. INSERT ACCOUNTS với password hash BCrypt
-- Password gốc: admin123, customer123
INSERT INTO accounts (account_id, account_name, account_phone, password, status) VALUES
('admin-acc-uuid-001', 'Admin System', '0901234567', '$2a$10$8K1p/wgoj2F.ZRyOIng2gOmKdscZseW25o5ykVybL1P6bKSGl8XxG', 1),
('customer1-uuid-002', 'Nguyễn Văn An', '0987654321', '$2a$10$8K1p/wgoj2F.ZRyOIng2gOmKdscZseW25o5ykVybL1P6bKSGl8XxG', 1),
('customer2-uuid-003', 'Trần Thị Bình', '0976543210', '$2a$10$8K1p/wgoj2F.ZRyOIng2gOmKdscZseW25o5ykVybL1P6bKSGl8XxG', 1)
ON CONFLICT (account_id) DO NOTHING;

-- 4. INSERT ACCOUNT_ROLES (phân quyền)
INSERT INTO account_roles (account_id, role_id) VALUES
('admin-acc-uuid-001', 'admin-role-uuid-001'),
('customer1-uuid-002', 'customer-role-uuid-002'), 
('customer2-uuid-003', 'customer-role-uuid-002')
ON CONFLICT (account_id, role_id) DO NOTHING;

-- 5. INSERT PRODUCTS
INSERT INTO products (product_id, product_name, price, stock, description, created_at, updated_at, status) VALUES
('product-apple-uuid-001', 'Táo Fuji Nhật Bản', 125000, 50, 'Táo Fuji nhập khẩu từ Nhật Bản, ngọt mát, giòn', NOW(), NOW(), 1),
('product-orange-uuid-002', 'Cam Sành Hà Giang', 85000, 30, 'Cam sành tự nhiên từ Hà Giang, nhiều vitamin C', NOW(), NOW(), 1),
('product-mango-uuid-003', 'Xoài Cát Chu Đồng Nai', 95000, 25, 'Xoài cát chu ngọt thơm đặc biệt từ Đồng Nai', NOW(), NOW(), 1)
ON CONFLICT (product_id) DO NOTHING;

-- 6. INSERT PRODUCT_CATEGORY  
INSERT INTO product_category (productid, categoryid) VALUES
('product-apple-uuid-001', 'cat-fruits-uuid-001'),
('product-orange-uuid-002', 'cat-fruits-uuid-001'),
('product-mango-uuid-003', 'cat-fruits-uuid-001')
ON CONFLICT (productid, categoryid) DO NOTHING;

-- 7. INSERT PRODUCT_IMAGES với URL Cloudinary thật
INSERT INTO product_images (image_url, image_order, is_main, product_id) VALUES
-- Táo Fuji
('https://res.cloudinary.com/djjfohr4t/image/upload/v1/fruits/apple_fuji_main.jpg', 1, true, 'product-apple-uuid-001'),
('https://res.cloudinary.com/djjfohr4t/image/upload/v1/fruits/apple_fuji_detail.jpg', 2, false, 'product-apple-uuid-001'),

-- Cam Sành  
('https://res.cloudinary.com/djjfohr4t/image/upload/v1/fruits/orange_sanh_main.jpg', 1, true, 'product-orange-uuid-002'),
('https://res.cloudinary.com/djjfohr4t/image/upload/v1/fruits/orange_sanh_detail.jpg', 2, false, 'product-orange-uuid-002'),

-- Xoài Cát Chu
('https://res.cloudinary.com/djjfohr4t/image/upload/v1/fruits/mango_catchu_main.jpg', 1, true, 'product-mango-uuid-003'),
('https://res.cloudinary.com/djjfohr4t/image/upload/v1/fruits/mango_catchu_detail.jpg', 2, false, 'product-mango-uuid-003');

-- 8. INSERT CARTS
INSERT INTO carts (cart_id, account_id) VALUES
('cart-customer1-uuid-001', 'customer1-uuid-002'),
('cart-customer2-uuid-002', 'customer2-uuid-003')
ON CONFLICT (cart_id) DO NOTHING;

-- ===============================
-- KIỂM TRA DỮ LIỆU & TEST LOGIN
-- ===============================

-- Kiểm tra dữ liệu tổng quan
SELECT 'ROLES' as table_name, COUNT(*) as count FROM roles
UNION ALL  
SELECT 'ACCOUNTS', COUNT(*) FROM accounts
UNION ALL
SELECT 'CATEGORIES', COUNT(*) FROM categories  
UNION ALL
SELECT 'PRODUCTS', COUNT(*) FROM products
UNION ALL
SELECT 'PRODUCT_IMAGES', COUNT(*) FROM product_images;

-- Xem account với role (test accounts)
SELECT a.account_name, a.account_phone, r.role_name, a.status
FROM accounts a
JOIN account_roles ar ON a.account_id = ar.account_id
JOIN roles r ON ar.role_id = r.role_id
ORDER BY a.account_name;

-- Xem products với images
SELECT p.product_name, p.price, p.stock, COUNT(pi.id) as image_count
FROM products p 
LEFT JOIN product_images pi ON p.product_id = pi.product_id
GROUP BY p.product_id, p.product_name, p.price, p.stock
ORDER BY p.product_name;

-- ===============================
-- THÔNG TIN ĐĂNG NHẬP TEST
-- ===============================
/*
ADMIN:
- Phone: 0901234567  
- Password: admin123

CUSTOMER 1:  
- Phone: 0987654321
- Password: customer123

CUSTOMER 2:
- Phone: 0976543210 
- Password: customer123

API Test:
POST /api/account/login
{
  "accountPhone": "0987654321",
  "password": "customer123"  
}
*/