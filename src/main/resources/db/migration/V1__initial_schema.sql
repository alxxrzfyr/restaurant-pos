CREATE TABLE users (
    id                     INTEGER PRIMARY KEY AUTOINCREMENT,
    username               TEXT NOT NULL UNIQUE,
    display_name           TEXT NOT NULL,
    password_hash          TEXT NOT NULL,
    role                   TEXT NOT NULL CHECK (role IN ('ADMINISTRATOR', 'CASHIER')),
    active                 INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    failed_login_attempts  INTEGER NOT NULL DEFAULT 0,
    last_failed_login_at_epoch_ms INTEGER,
    locked_until_epoch_ms  INTEGER,
    created_at_epoch_ms    INTEGER NOT NULL,
    updated_at_epoch_ms    INTEGER NOT NULL
) STRICT;

CREATE INDEX idx_users_username ON users(username);

CREATE TABLE categories (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    name           TEXT NOT NULL UNIQUE,
    display_order  INTEGER NOT NULL DEFAULT 0
) STRICT;

CREATE TABLE menu_items (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    name             TEXT NOT NULL,
    price_minor      INTEGER NOT NULL CHECK (price_minor >= 0),
    cost_minor       INTEGER CHECK (cost_minor IS NULL OR cost_minor >= 0),
    category_id      INTEGER NOT NULL REFERENCES categories(id),
    available        INTEGER NOT NULL DEFAULT 1 CHECK (available IN (0, 1)),
    created_at_epoch_ms INTEGER NOT NULL,
    updated_at_epoch_ms INTEGER NOT NULL
) STRICT;

CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_menu_items_available ON menu_items(available);

CREATE TABLE orders (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    cashier_id           INTEGER NOT NULL REFERENCES users(id),
    order_type           TEXT NOT NULL CHECK (order_type IN ('DINE_IN', 'TAKE_OUT')),
    table_number         TEXT,
    notes                TEXT,
    subtotal_minor       INTEGER NOT NULL CHECK (subtotal_minor >= 0),
    discount_minor       INTEGER NOT NULL DEFAULT 0 CHECK (discount_minor >= 0),
    vat_rate_percent     TEXT NOT NULL,
    vat_minor            INTEGER NOT NULL CHECK (vat_minor >= 0),
    total_due_minor      INTEGER NOT NULL CHECK (total_due_minor >= 0),
    status               TEXT NOT NULL CHECK (status IN ('OPEN', 'PAID', 'VOIDED')),
    placed_at_epoch_ms   INTEGER NOT NULL,
    voided_at_epoch_ms   INTEGER,
    voided_by_user_id    INTEGER REFERENCES users(id),
    void_reason          TEXT
) STRICT;

CREATE INDEX idx_orders_placed_at ON orders(placed_at_epoch_ms);
CREATE INDEX idx_orders_cashier ON orders(cashier_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE order_line_items (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id       INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id   INTEGER REFERENCES menu_items(id),
    item_name      TEXT NOT NULL,
    unit_price_minor INTEGER NOT NULL CHECK (unit_price_minor >= 0),
    quantity       INTEGER NOT NULL CHECK (quantity > 0)
) STRICT;

CREATE INDEX idx_order_line_items_order ON order_line_items(order_id);

CREATE TABLE payments (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id           INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    method             TEXT NOT NULL CHECK (method IN ('CASH', 'DEBIT_CARD', 'CREDIT_CARD')),
    amount_tendered_minor INTEGER NOT NULL CHECK (amount_tendered_minor >= 0),
    change_given_minor INTEGER NOT NULL CHECK (change_given_minor >= 0),
    paid_at_epoch_ms   INTEGER NOT NULL
) STRICT;

CREATE INDEX idx_payments_order ON payments(order_id);

CREATE TABLE settings (
    key    TEXT PRIMARY KEY,
    value  TEXT NOT NULL
) STRICT;

CREATE TABLE audit_events (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    occurred_at_epoch_ms INTEGER NOT NULL,
    user_id           INTEGER REFERENCES users(id),
    username          TEXT,
    event_type        TEXT NOT NULL,
    details           TEXT
) STRICT;

CREATE INDEX idx_audit_events_occurred_at ON audit_events(occurred_at_epoch_ms);
CREATE INDEX idx_audit_events_event_type ON audit_events(event_type);
