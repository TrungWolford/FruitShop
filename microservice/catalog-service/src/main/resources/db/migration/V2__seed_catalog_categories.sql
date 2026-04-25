INSERT INTO categories (category_id, category_name, status)
VALUES
    ('33333333-3333-3333-3333-333333333333', 'Trai cay tuoi', 1),
    ('44444444-4444-4444-4444-444444444444', 'Trai cay nhap khau', 1)
ON CONFLICT (category_name) DO NOTHING;
