ALTER TABLE users ADD COLUMN requires_password_change INTEGER NOT NULL DEFAULT 0;
UPDATE users SET requires_password_change = 1 WHERE username IN ('admin', 'cashier');
