ALTER TABLE recurring_expenses
    ADD COLUMN payment_method VARCHAR(30) NOT NULL DEFAULT 'PIX' AFTER account_id,
    ADD COLUMN credit_card_id CHAR(36) NULL AFTER payment_method,
    ADD COLUMN last_paid_year INT NULL AFTER recurrence,
    ADD COLUMN last_paid_month INT NULL AFTER last_paid_year;

ALTER TABLE recurring_expenses
    ADD KEY idx_recurring_expenses_credit_card_id (credit_card_id),
    ADD CONSTRAINT fk_recurring_expenses_credit_card
        FOREIGN KEY (credit_card_id) REFERENCES credit_cards (id);
