ALTER TABLE purchases
    ADD COLUMN purchase_time TIME NULL;

UPDATE purchases
SET purchase_time = TIME(created_at)
WHERE purchase_time IS NULL;
