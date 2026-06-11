-- ============================================================
-- ExpensePro - Smart Expense Tracker v2
-- MySQL 8.0+ Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS expense_pro;
USE expense_pro;

-- ── Users ──────────────────────────────────────────────────
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    avatar_color  VARCHAR(20)  DEFAULT '#6366f1',
    currency      VARCHAR(10)  DEFAULT 'INR',
    monthly_limit DECIMAL(15,2) DEFAULT 0.00,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active     BOOLEAN DEFAULT TRUE
);

-- ── Categories ─────────────────────────────────────────────
CREATE TABLE categories (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(60)  NOT NULL,
    icon       VARCHAR(10)  DEFAULT '📦',
    color      VARCHAR(20)  DEFAULT '#6366f1',
    user_id    BIGINT,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ── Expenses ───────────────────────────────────────────────
CREATE TABLE expenses (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT        NOT NULL,
    category_id    BIGINT        NOT NULL,
    title          VARCHAR(120)  NOT NULL,
    note           TEXT,
    amount         DECIMAL(15,2) NOT NULL,
    expense_date   DATE          NOT NULL,
    payment_mode   ENUM('CASH','UPI','CARD','NET_BANKING','WALLET','OTHER') DEFAULT 'CASH',
    is_recurring   BOOLEAN DEFAULT FALSE,
    receipt_url    VARCHAR(500),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)     REFERENCES users(id)      ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

-- ── Budgets (per category per month) ───────────────────────
CREATE TABLE budgets (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    category_id BIGINT        NOT NULL,
    amount      DECIMAL(15,2) NOT NULL,
    month       TINYINT       NOT NULL,
    year        SMALLINT      NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_budget (user_id, category_id, month, year),
    FOREIGN KEY (user_id)     REFERENCES users(id)      ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- ── Savings Goals ──────────────────────────────────────────
CREATE TABLE savings_goals (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT        NOT NULL,
    title        VARCHAR(100)  NOT NULL,
    target_amount DECIMAL(15,2) NOT NULL,
    saved_amount  DECIMAL(15,2) DEFAULT 0.00,
    deadline     DATE,
    icon         VARCHAR(10)   DEFAULT '🎯',
    color        VARCHAR(20)   DEFAULT '#10b981',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ── Indexes ────────────────────────────────────────────────
CREATE INDEX idx_exp_user_date  ON expenses(user_id, expense_date);
CREATE INDEX idx_exp_cat        ON expenses(category_id);
CREATE INDEX idx_exp_user       ON expenses(user_id);

-- ── Seed Default Categories ────────────────────────────────
INSERT INTO categories (name, icon, color, is_default) VALUES
('Food & Dining',    '🍽️',  '#f97316', TRUE),
('Transportation',   '🚗',  '#3b82f6', TRUE),
('Shopping',         '🛍️',  '#ec4899', TRUE),
('Entertainment',    '🎬',  '#8b5cf6', TRUE),
('Health',           '💊',  '#ef4444', TRUE),
('Bills & Utilities','⚡',  '#06b6d4', TRUE),
('Education',        '📚',  '#10b981', TRUE),
('Travel',           '✈️',  '#f59e0b', TRUE),
('Groceries',        '🛒',  '#84cc16', TRUE),
('Investment',       '📈',  '#6366f1', TRUE),
('Rent & Housing',   '🏠',  '#14b8a6', TRUE),
('Miscellaneous',    '📦',  '#94a3b8', TRUE);

-- ── Useful Views ───────────────────────────────────────────
CREATE VIEW v_monthly_summary AS
SELECT
    e.user_id,
    YEAR(e.expense_date)  AS yr,
    MONTH(e.expense_date) AS mo,
    c.id    AS category_id,
    c.name  AS category_name,
    c.icon  AS category_icon,
    c.color AS category_color,
    COUNT(*)        AS txn_count,
    SUM(e.amount)   AS total
FROM expenses e
JOIN categories c ON c.id = e.category_id
GROUP BY e.user_id, yr, mo, c.id;
