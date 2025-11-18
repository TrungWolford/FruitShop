-- Import data for H2 database

INSERT INTO roles (role_id, role_name) VALUES
('RO366D82', 'CUSTOMER'),
('RO3CLVSD', 'STAFF'),
('ROPYVQV2', 'ADMIN');

INSERT INTO accounts (account_id, account_name, account_phone, password, status) VALUES
('AC2SE64SH3', 'Customer 1', '0123456789', '$2a$10$dummyHashPassword', 1),
('ACABT7AI4M', 'Admin 1', '0987654321', '$2a$10$dummyHashPassword', 1),
('ACPC0QNAQD', 'Admin 2', '0912345678', '$2a$10$dummyHashPassword', 1),
('ACUUIK5DD0', 'Customer 2', '0934567890', '$2a$10$dummyHashPassword', 1),
('ACYJOJXM0B', 'Customer 3', '0945678901', '$2a$10$dummyHashPassword', 1);

INSERT INTO account_roles (account_id, role_id) VALUES
('AC2SE64SH3', 'RO366D82'),
('ACABT7AI4M', 'ROPYVQV2'),
('ACPC0QNAQD', 'ROPYVQV2'),
('ACUUIK5DD0', 'RO366D82'),
('ACYJOJXM0B', 'RO366D82');

INSERT INTO categories (category_id, category_name, status) VALUES
('C3E216K', 'Truyện cổ tích, ngụ ngôn', 1),
('C6CRI4F', 'Sách giáo dục thiếu nhi', 1),
('C7G4S0P', 'Sách tài chính – đầu tư', 1),
('C8ES8Q6', 'Kỹ năng sống, kỹ năng mềm', 1),
('C8G0KJ9', 'Kinh doanh, quản trị, khởi nghiệp', 1),
('C8TLBZG', 'Du ký, hồi ký, tự truyện', 1),
('C8VWVMW', 'Sách khoa học', 1),
('C9PZL0O', 'Sách tâm lý học', 1),
('CBZ2C98', 'Sách khoa học viễn tưởng', 1),
('CCJ234C', 'Tiểu thuyết', 1),
('CF2ZRP4', 'Văn hóa – Nghệ thuật', 1),
('CH6Q8FH', 'Truyện ngắn, tản văn', 1),
('CLQLL8J', 'Thơ ca', 1),
('CLSV7KP', 'Truyện tranh, manga, comic', 1),
('CN3883N', 'Kịch', 1),
('COK9WIC', 'Sách giáo khoa, tham khảo', 1),
('CRC54ME', 'Triết học – Tôn giáo', 1),
('CSOD6PI', 'Lịch sử – Địa lý', 1),
('CSRT85L', 'Chính trị – Xã hội – Pháp luật', 1),
('CV75BVL', 'Tâm lý học ứng dụng', 1);

-- Note: Chỉ import một phần products do giới hạn độ dài. Thêm phần còn lại tương tự.
INSERT INTO products (product_id, created_at, description, price, product_name, status, stock) VALUES
('0CCVGS0A', '2025-09-16 16:27:22.000000', 'Sách Thiên tài bên trái kẻ điên bên phải của Nguyễn Duy Cần, nội dung hấp dẫn.', 123307, 'Thiên tài bên trái kẻ điên bên phải', 1, 111),
('14K12Y6H', '2025-09-16 16:27:22.000000', 'Sách Muôn kiếp nhân sinh của Dale Carnegie, nội dung hấp dẫn.', 327334, 'Muôn kiếp nhân sinh', 1, 90),
('18O1OKR0', '2025-09-16 16:27:22.000000', 'Sách Người nam châm của Paulo Coelho, nội dung hấp dẫn.', 67900, 'Người nam châm', 1, 168);
