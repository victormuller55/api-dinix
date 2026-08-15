CREATE TABLE credit_card_invoices (
    id CHAR(36) NOT NULL,
    credit_card_id CHAR(36) NOT NULL,
    reference_year INT NOT NULL,
    reference_month INT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_credit_card_invoices_card_period (credit_card_id, reference_year, reference_month),
    KEY idx_credit_card_invoices_card_id (credit_card_id),
    KEY idx_credit_card_invoices_status (status),
    CONSTRAINT chk_credit_card_invoices_month CHECK (reference_month BETWEEN 1 AND 12),
    CONSTRAINT chk_credit_card_invoices_status CHECK (status IN ('CURRENT', 'UPCOMING', 'CLOSED', 'PAID')),
    CONSTRAINT fk_credit_card_invoices_card FOREIGN KEY (credit_card_id) REFERENCES credit_cards (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
