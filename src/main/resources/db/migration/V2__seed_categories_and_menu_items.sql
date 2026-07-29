
INSERT INTO categories (name, display_order) VALUES
    ('Meals', 1),
    ('Drinks', 2),
    ('Desserts', 3);

INSERT INTO menu_items (name, price_minor, category_id, available, created_at_epoch_ms, updated_at_epoch_ms)
SELECT 'Burger', 3000, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Meals'
UNION ALL
SELECT 'Pizza', 11000, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Meals'
UNION ALL
SELECT 'Pasta', 9500, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Meals'
UNION ALL
SELECT 'Salad', 13500, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Meals'
UNION ALL
SELECT 'Steak', 25000, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Meals'
UNION ALL
SELECT 'Water', 1500, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Drinks'
UNION ALL
SELECT 'Soda', 2500, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Drinks'
UNION ALL
SELECT 'Iced Tea', 3550, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Drinks'
UNION ALL
SELECT 'Lemonade', 3000, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Drinks'
UNION ALL
SELECT 'Coffee', 5500, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Drinks'
UNION ALL
SELECT 'Cheesecake', 6500, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Desserts'
UNION ALL
SELECT 'Ice Cream', 3050, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Desserts'
UNION ALL
SELECT 'Chocolate Cake', 6000, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Desserts'
UNION ALL
SELECT 'Fruit Salad', 4550, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Desserts'
UNION ALL
SELECT 'Tiramisu', 10000, id, 1, unixepoch('now') * 1000, unixepoch('now') * 1000 FROM categories WHERE name = 'Desserts';

INSERT INTO settings (key, value) VALUES
    ('business.name', 'Restaurant POS'),
    ('business.address', ''),
    ('business.phone', ''),
    ('business.vatRatePercent', '12.00');
