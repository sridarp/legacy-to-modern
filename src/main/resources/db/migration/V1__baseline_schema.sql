-- V1: Baseline schema for Firefly III modernized
-- Aligned with legacy PHP Firefly III database structure

-- Account types lookup
CREATE TABLE IF NOT EXISTS account_types (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type        VARCHAR(50) NOT NULL UNIQUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO account_types (type) VALUES
    ('Asset account'), ('Expense account'), ('Revenue account'),
    ('Cash account'), ('Initial balance account'), ('Beneficiary account'),
    ('Import account'), ('Loan'), ('Debt'), ('Mortgage'),
    ('Reconciliation account'), ('Liability credit account'), ('Credit card')
ON CONFLICT (type) DO NOTHING;

-- Transaction types lookup
CREATE TABLE IF NOT EXISTS transaction_types (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type        VARCHAR(50) NOT NULL UNIQUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO transaction_types (type) VALUES
    ('Withdrawal'), ('Deposit'), ('Transfer'),
    ('Opening balance'), ('Reconciliation'), ('Liability credit'), ('Invalid')
ON CONFLICT (type) DO NOTHING;

-- Transaction currencies
CREATE TABLE IF NOT EXISTS transaction_currencies (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code            VARCHAR(10) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    symbol          VARCHAR(10) NOT NULL DEFAULT '',
    decimal_places  INT NOT NULL DEFAULT 2,
    enabled         BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO transaction_currencies (code, name, symbol, decimal_places) VALUES
    ('EUR', 'Euro', '€', 2),
    ('USD', 'US Dollar', '$', 2),
    ('GBP', 'British Pound', '£', 2)
ON CONFLICT (code) DO NOTHING;

-- User groups (multi-tenancy)
CREATE TABLE IF NOT EXISTS user_groups (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_group_id   BIGINT REFERENCES user_groups(id),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(50) NOT NULL DEFAULT 'USER',
    enabled         BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Accounts
CREATE TABLE IF NOT EXISTS accounts (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_group_id           BIGINT REFERENCES user_groups(id),
    account_type_id         BIGINT NOT NULL REFERENCES account_types(id),
    name                    VARCHAR(255) NOT NULL,
    active                  BOOLEAN NOT NULL DEFAULT true,
    virtual_balance         DECIMAL(32, 12) DEFAULT NULL,
    native_virtual_balance  DECIMAL(32, 12) DEFAULT NULL,
    iban                    VARCHAR(255) DEFAULT NULL,
    order_col               INT NOT NULL DEFAULT 0,
    deleted_at              TIMESTAMP DEFAULT NULL,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_user_group_id ON accounts(user_group_id);
CREATE INDEX IF NOT EXISTS idx_accounts_type ON accounts(account_type_id);

-- Account metadata
CREATE TABLE IF NOT EXISTS account_meta (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id  BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    data        TEXT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transaction groups (split transactions)
CREATE TABLE IF NOT EXISTS transaction_groups (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_group_id   BIGINT REFERENCES user_groups(id),
    title           VARCHAR(255) DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transaction journals (double-entry header)
CREATE TABLE IF NOT EXISTS transaction_journals (
    id                          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id                     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_group_id               BIGINT REFERENCES user_groups(id),
    transaction_type_id         BIGINT NOT NULL REFERENCES transaction_types(id),
    transaction_group_id        BIGINT REFERENCES transaction_groups(id),
    transaction_currency_id     BIGINT REFERENCES transaction_currencies(id),
    bill_id                     BIGINT DEFAULT NULL,
    description                 VARCHAR(1024) NOT NULL,
    date                        TIMESTAMP NOT NULL,
    date_tz                     VARCHAR(100) DEFAULT 'UTC',
    order_col                   INT NOT NULL DEFAULT 0,
    tag_count                   INT NOT NULL DEFAULT 0,
    completed                   BOOLEAN NOT NULL DEFAULT true,
    deleted_at                  TIMESTAMP DEFAULT NULL,
    created_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tj_user_id ON transaction_journals(user_id);
CREATE INDEX IF NOT EXISTS idx_tj_type ON transaction_journals(transaction_type_id);
CREATE INDEX IF NOT EXISTS idx_tj_group ON transaction_journals(transaction_group_id);
CREATE INDEX IF NOT EXISTS idx_tj_date ON transaction_journals(date);

-- Transactions (individual splits of a journal — double-entry legs)
CREATE TABLE IF NOT EXISTS transactions (
    id                          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id                  BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    transaction_journal_id      BIGINT NOT NULL REFERENCES transaction_journals(id) ON DELETE CASCADE,
    transaction_currency_id     BIGINT REFERENCES transaction_currencies(id),
    description                 VARCHAR(1024) DEFAULT NULL,
    amount                      DECIMAL(32, 12) NOT NULL,
    native_amount               DECIMAL(32, 12) DEFAULT NULL,
    foreign_currency_id         BIGINT REFERENCES transaction_currencies(id),
    foreign_amount              DECIMAL(32, 12) DEFAULT NULL,
    native_foreign_amount       DECIMAL(32, 12) DEFAULT NULL,
    reconciled                  BOOLEAN NOT NULL DEFAULT false,
    identifier                  INT NOT NULL DEFAULT 0,
    deleted_at                  TIMESTAMP DEFAULT NULL,
    created_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tx_account ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_tx_journal ON transactions(transaction_journal_id);

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_group_id   BIGINT REFERENCES user_groups(id),
    name            VARCHAR(255) NOT NULL,
    deleted_at      TIMESTAMP DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Budgets
CREATE TABLE IF NOT EXISTS budgets (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_group_id   BIGINT REFERENCES user_groups(id),
    name            VARCHAR(255) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT true,
    order_col       INT NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMP DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Budget limits
CREATE TABLE IF NOT EXISTS budget_limits (
    id                          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    budget_id                   BIGINT NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
    transaction_currency_id     BIGINT REFERENCES transaction_currencies(id),
    start_date                  DATE NOT NULL,
    end_date                    DATE DEFAULT NULL,
    amount                      DECIMAL(32, 12) NOT NULL,
    period                      VARCHAR(50) DEFAULT NULL,
    generated                   BOOLEAN NOT NULL DEFAULT false,
    created_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bills / subscriptions
CREATE TABLE IF NOT EXISTS bills (
    id                          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id                     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_group_id               BIGINT REFERENCES user_groups(id),
    transaction_currency_id     BIGINT REFERENCES transaction_currencies(id),
    name                        VARCHAR(255) NOT NULL,
    amount_min                  DECIMAL(32, 12) NOT NULL,
    amount_max                  DECIMAL(32, 12) NOT NULL,
    date                        TIMESTAMP NOT NULL,
    date_tz                     VARCHAR(100) DEFAULT 'UTC',
    end_date                    TIMESTAMP DEFAULT NULL,
    extension_date              TIMESTAMP DEFAULT NULL,
    repeat_freq                 VARCHAR(50) NOT NULL DEFAULT 'monthly',
    skip                        INT NOT NULL DEFAULT 0,
    automatch                   BOOLEAN NOT NULL DEFAULT true,
    active                      BOOLEAN NOT NULL DEFAULT true,
    order_col                   INT NOT NULL DEFAULT 0,
    deleted_at                  TIMESTAMP DEFAULT NULL,
    created_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tags
CREATE TABLE IF NOT EXISTS tags (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_group_id   BIGINT REFERENCES user_groups(id),
    tag             VARCHAR(255) NOT NULL,
    tag_mode        VARCHAR(50) NOT NULL DEFAULT 'nothing',
    date            DATE DEFAULT NULL,
    description     TEXT DEFAULT NULL,
    latitude        DECIMAL(18,12) DEFAULT NULL,
    longitude       DECIMAL(18,12) DEFAULT NULL,
    zoom_level      INT DEFAULT NULL,
    deleted_at      TIMESTAMP DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Join: budget_transaction_journal
CREATE TABLE IF NOT EXISTS budget_transaction_journal (
    budget_id               BIGINT NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
    transaction_journal_id  BIGINT NOT NULL REFERENCES transaction_journals(id) ON DELETE CASCADE,
    PRIMARY KEY (budget_id, transaction_journal_id)
);

-- Join: category_transaction_journal
CREATE TABLE IF NOT EXISTS category_transaction_journal (
    category_id             BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    transaction_journal_id  BIGINT NOT NULL REFERENCES transaction_journals(id) ON DELETE CASCADE,
    PRIMARY KEY (category_id, transaction_journal_id)
);

-- Seed default user group and admin user
INSERT INTO user_groups (title) VALUES ('Default Group') ON CONFLICT DO NOTHING;

-- Default admin user with bcrypt hash of 'changeme'
INSERT INTO users (user_group_id, email, password, role) VALUES
    (1, 'admin@firefly.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN')
ON CONFLICT (email) DO NOTHING;