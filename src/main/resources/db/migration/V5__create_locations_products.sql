CREATE TABLE purchase_locations (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    name VARCHAR(180) NOT NULL,
    description VARCHAR(255),
    address VARCHAR(255),
    city VARCHAR(120),
    state VARCHAR(2),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_purchase_locations_user_id (user_id),
    CONSTRAINT fk_purchase_locations_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    name VARCHAR(180) NOT NULL,
    description VARCHAR(255),
    brand VARCHAR(120),
    category_id CHAR(36),
    average_price DECIMAL(19, 2),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_products_user_id (user_id),
    KEY idx_products_category_id (category_id),
    CONSTRAINT fk_products_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
