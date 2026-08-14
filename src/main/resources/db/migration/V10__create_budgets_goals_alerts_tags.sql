CREATE TABLE budgets (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    category_id CHAR(36) NOT NULL,
    amount_limit DECIMAL(19, 2) NOT NULL,
    period_month INT NOT NULL,
    period_year INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_budget_user_category_period (user_id, category_id, period_month, period_year),
    KEY idx_budgets_user_id (user_id),
    KEY idx_budgets_category_id (category_id),
    CONSTRAINT chk_budget_month CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE financial_goals (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    name VARCHAR(180) NOT NULL,
    description VARCHAR(255),
    target_amount DECIMAL(19, 2) NOT NULL,
    current_amount DECIMAL(19, 2) NOT NULL DEFAULT 0,
    target_date DATE,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_financial_goals_user_id (user_id),
    CONSTRAINT fk_financial_goals_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE financial_alerts (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000),
    amount DECIMAL(19, 2),
    due_date DATE,
    read_flag TINYINT(1) NOT NULL DEFAULT 0,
    reference_key VARCHAR(80),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_alerts_user_id (user_id),
    KEY idx_alerts_user_read (user_id, read_flag),
    UNIQUE KEY uk_alerts_user_reference (user_id, reference_key),
    CONSTRAINT fk_alerts_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tags (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    name VARCHAR(60) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_user_name (user_id, name),
    KEY idx_tags_user_id (user_id),
    CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_tags (
    purchase_id CHAR(36) NOT NULL,
    tag_id CHAR(36) NOT NULL,
    PRIMARY KEY (purchase_id, tag_id),
    CONSTRAINT fk_purchase_tags_purchase FOREIGN KEY (purchase_id) REFERENCES purchases (id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attachments (
    id CHAR(36) NOT NULL,
    transaction_id CHAR(36) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    content_type VARCHAR(120),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_attachments_transaction_id (transaction_id),
    CONSTRAINT fk_attachments_transaction FOREIGN KEY (transaction_id) REFERENCES financial_transactions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE idempotency_keys (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    idempotency_key VARCHAR(80) NOT NULL,
    request_hash VARCHAR(120) NOT NULL,
    response_body LONGTEXT NOT NULL,
    status_code INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_idempotency_user_key (user_id, idempotency_key),
    KEY idx_idempotency_user_id (user_id),
    CONSTRAINT fk_idempotency_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE net_worth_snapshots (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    period_month INT NOT NULL,
    period_year INT NOT NULL,
    accounts_balance DECIMAL(19, 2) NOT NULL,
    investments_value DECIMAL(19, 2) NOT NULL,
    debts DECIMAL(19, 2) NOT NULL,
    net_worth DECIMAL(19, 2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_net_worth_user_period (user_id, period_month, period_year),
    KEY idx_net_worth_user_id (user_id),
    CONSTRAINT fk_net_worth_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
