ALTER TABLE purchase_locations
    ADD COLUMN category_id CHAR(36) NULL AFTER description,
    ADD KEY idx_purchase_locations_category_id (category_id),
    ADD CONSTRAINT fk_purchase_locations_category
        FOREIGN KEY (category_id) REFERENCES categories (id);
