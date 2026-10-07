-- =============================================================================
-- DATA SEED SCRIPT: THÊM 20 SẢN PHẨM HOẠ QUẢ (FRUITSHOP)
-- Database: PostgreSQL / MySQL / H2
-- Target Tables: categories, products, product_category, product_images
-- =============================================================================

-- 1. Thêm các danh mục hoa quả (nếu chưa có)
INSERT INTO categories (category_id, category_name, status) VALUES
    ('CAT_FRUIT_01', 'Trái cây nhập khẩu', 1),
    ('CAT_FRUIT_02', 'Trái cây nội địa', 1),
    ('CAT_FRUIT_03', 'Trái cây sấy khô', 1),
    ('CAT_FRUIT_04', 'Nước ép & Sinh tố', 1),
    ('CAT_FRUIT_05', 'Giỏ quà trái cây', 1)
ON CONFLICT (category_id) DO NOTHING;

-- 2. Thêm 20 sản phẩm hoa quả vào bảng `products`
INSERT INTO products (product_id, product_name, price, stock, description, status, created_at, updated_at) VALUES
    ('FRT00001', 'Táo Envy Mỹ (Size L)', 195000, 50, 'Táo Envy nhập khẩu trực tiếp từ Mỹ, giòn ngọt đậm đà, thơm đặc trưng, vỏ đỏ tươi và thịt quả trắng mịn.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00002', 'Sầu Riêng Ri6 Chín Cây (Trái 3kg)', 160000, 30, 'Sầu riêng Ri6 Miền Tây chín cây tự nhiên, cơm vàng hạt lép, béo ngậy thơm lừng.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00003', 'Nho Mẫu Đơn Nhật Bản (Shine Muscat)', 450000, 20, 'Nho mẫu đơn chùm to tròn, giòn ngọt, hương thơm hoa sữa đặc trưng, không hạt.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00004', 'Xoài Cát Hòa Lộc Loại 1', 85000, 60, 'Xoài Cát Hòa Lộc Tiền Giang chuẩn VietGAP, trái to đều, thịt dày, ngọt thanh đậm đà.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00005', 'Cam Vàng Navel Úc', 95000, 80, 'Cam vàng Navel Úc vỏ mỏng, mọng nước, vị ngọt dịu tự nhiên, bổ sung vitamin C dồi dào.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00006', 'Măng Cụt Bến Tre Chín Cây', 120000, 40, 'Măng cụt ruột trắng trong, vị chua ngọt hài hòa, vỏ mỏng nhiều múi không hạt.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00007', 'Dâu Tây Sơn La Hộp 500g', 135000, 45, 'Dâu tây Mộc Châu chín đỏ mọng, thơm nồng quyến rũ, đóng hộp giữ trọn độ tươi.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00008', 'Việt Quất Tươi New Zealand 125g', 110000, 50, 'Việt quất tươi New Zealand giàu chất chống oxy hóa, quả to mọng, vị chua nhẹ thanh mát.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00009', 'Bưởi Da Xanh Bến Tre Loại 1', 75000, 70, 'Bưởi da xanh ruột hồng, múi mọng nước, tép bưởi giòn ngọt không đắng tép.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00010', 'Kiwi Vàng SunGold New Zealand', 180000, 35, 'Kiwi vàng vị ngọt lịm như mật, giàu vitamin C và chất xơ, vỏ mịn ruột vàng ươm.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00011', 'Thanh Long Ruột Đỏ Bình Thuận', 45000, 100, 'Thanh long ruột đỏ tươi ngon giàu dinh dưỡng, vị ngọt đậm, làm đẹp da và tốt cho sức khỏe.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00012', 'Nước Ép Cam Tươi Nguyên Chất 330ml', 35000, 150, 'Nước ép cam nguyên chất 100% không đường, ép tươi mỗi ngày giữ trọn vitamin.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00013', 'Nước Ép Táo & Dâu Tươi 330ml', 42000, 120, 'Sự kết hợp hoàn hảo giữa vị ngọt thanh của táo và vị thơm dịu của dâu tây tươi.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00014', 'Xoài Sấy Dẻo Xuất Khẩu 200g', 65000, 90, 'Xoài sấy dẻo công nghệ sấy lạnh giữ nguyên hương vị xoài chín, dẻo ngọt tự nhiên không chất bảo quản.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00015', 'Mít Sấy Giòn Đặc Sản 250g', 55000, 110, 'Mít sấy giòn rụm thơm lừng, làm từ mít thái tươi chọn lọc, thơm ngon giòn tan.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00016', 'Giỏ Trái Cây Ngũ Quả Biếu Tặng', 850000, 15, 'Giỏ quà cao cấp gồm Táo Envy, Nho Mẫu Đơn, Cam Vàng, Lê Hàn Quốc và Kiwi Vàng, kết nơ sang trọng.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00017', 'Giỏ Trái Cây Đóng Hộp Premium', 1200000, 10, 'Giỏ quà trái cây nhập khẩu tuyển chọn 100%, thiết kế tinh tế thích hợp biếu tặng đối tác, người thân.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00018', 'Lê Nâu Hàn Quốc (Size L)', 140000, 40, 'Lê nâu Hàn Quốc quả to tròn, vỏ vàng nâu, thịt trắng giòn nhiều nước, vị ngọt mát giải nhiệt.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00019', 'Bơ Sáp 034 Lâm Đồng', 60000, 65, 'Bơ sáp 034 trái dài, cơm vàng dẻo quánh, hạt nhỏ, vị béo bậy đặc trưng Bảo Lộc.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FRT00020', 'Dưa Lưới Hoàng Kim VietGAP', 89000, 55, 'Dưa lưới Hoàng Kim trồng nhà màng, ruột cam giòn ngọt độ đường brix cao, vỏ vàng vân lưới đẹp.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (product_id) DO NOTHING;

-- 3. Liên kết sản phẩm với danh mục tương ứng trong `product_category`
INSERT INTO product_category (productid, categoryid) VALUES
    ('FRT00001', 'CAT_FRUIT_01'), -- Táo Envy -> Trái cây nhập khẩu
    ('FRT00002', 'CAT_FRUIT_02'), -- Sầu Riêng -> Trái cây nội địa
    ('FRT00003', 'CAT_FRUIT_01'), -- Nho Mẫu Đơn -> Trái cây nhập khẩu
    ('FRT00004', 'CAT_FRUIT_02'), -- Xoài Cát -> Trái cây nội địa
    ('FRT00005', 'CAT_FRUIT_01'), -- Cam Vàng -> Trái cây nhập khẩu
    ('FRT00006', 'CAT_FRUIT_02'), -- Măng Cụt -> Trái cây nội địa
    ('FRT00007', 'CAT_FRUIT_02'), -- Dâu Tây -> Trái cây nội địa
    ('FRT00008', 'CAT_FRUIT_01'), -- Việt Quất -> Trái cây nhập khẩu
    ('FRT00009', 'CAT_FRUIT_02'), -- Bưởi Da Xanh -> Trái cây nội địa
    ('FRT00010', 'CAT_FRUIT_01'), -- Kiwi Vàng -> Trái cây nhập khẩu
    ('FRT00011', 'CAT_FRUIT_02'), -- Thanh Long -> Trái cây nội địa
    ('FRT00012', 'CAT_FRUIT_04'), -- Nước Ép Cam -> Nước ép & Sinh tố
    ('FRT00013', 'CAT_FRUIT_04'), -- Nước Ép Táo & Dâu -> Nước ép & Sinh tố
    ('FRT00014', 'CAT_FRUIT_03'), -- Xoài Sấy Dẻo -> Trái cây sấy khô
    ('FRT00015', 'CAT_FRUIT_03'), -- Mít Sấy Giòn -> Trái cây sấy khô
    ('FRT00016', 'CAT_FRUIT_05'), -- Giỏ Trái Cây Ngũ Quả -> Giỏ quà trái cây
    ('FRT00017', 'CAT_FRUIT_05'), -- Giỏ Trái Cây Premium -> Giỏ quà trái cây
    ('FRT00018', 'CAT_FRUIT_01'), -- Lê Nâu -> Trái cây nhập khẩu
    ('FRT00019', 'CAT_FRUIT_02'), -- Bơ Sáp -> Trái cây nội địa
    ('FRT00020', 'CAT_FRUIT_02')  -- Dưa Lưới -> Trái cây nội địa
ON CONFLICT (productid, categoryid) DO NOTHING;

-- 4. Thêm hình ảnh sản phẩm tượng trưng vào `product_images`
INSERT INTO product_images (image_url, image_order, is_main, product_id) VALUES
    ('https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6', 0, true, 'FRT00001'),
    ('https://images.unsplash.com/photo-1587132137056-bfbf0166836e', 0, true, 'FRT00002'),
    ('https://images.unsplash.com/photo-1537640538966-79f369143f8f', 0, true, 'FRT00003'),
    ('https://images.unsplash.com/photo-1553279768-865429fa0078', 0, true, 'FRT00004'),
    ('https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b', 0, true, 'FRT00005'),
    ('https://images.unsplash.com/photo-1567306301408-9b74779a11af', 0, true, 'FRT00006'),
    ('https://images.unsplash.com/photo-1464965911861-746a04b4bca6', 0, true, 'FRT00007'),
    ('https://images.unsplash.com/photo-1498557850523-fd3d118b962e', 0, true, 'FRT00008'),
    ('https://images.unsplash.com/photo-1582979512210-99b6a53386f9', 0, true, 'FRT00009'),
    ('https://images.unsplash.com/photo-1585059819970-31398d66141a', 0, true, 'FRT00010'),
    ('https://images.unsplash.com/photo-1527325678964-549216468488', 0, true, 'FRT00011'),
    ('https://images.unsplash.com/photo-1613478223719-2ab802602423', 0, true, 'FRT00012'),
    ('https://images.unsplash.com/photo-1621506289937-a8e4df240d0b', 0, true, 'FRT00013'),
    ('https://images.unsplash.com/photo-1601004890684-d8cbf643f5f2', 0, true, 'FRT00014'),
    ('https://images.unsplash.com/photo-1596591606975-97ee5cef3a1e', 0, true, 'FRT00015'),
    ('https://images.unsplash.com/photo-1519996529931-28324d5a630e', 0, true, 'FRT00016'),
    ('https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e', 0, true, 'FRT00017'),
    ('https://images.unsplash.com/photo-1615485290382-441e4d049cb5', 0, true, 'FRT00018'),
    ('https://images.unsplash.com/photo-1523049673857-eb18f1d7b578', 0, true, 'FRT00019'),
    ('https://images.unsplash.com/photo-1595855759920-86582396756a', 0, true, 'FRT00020');
