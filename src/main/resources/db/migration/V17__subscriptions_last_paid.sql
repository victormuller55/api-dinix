ALTER TABLE subscriptions
    ADD COLUMN last_paid_year INT NULL AFTER next_billing_date,
    ADD COLUMN last_paid_month INT NULL AFTER last_paid_year;
