CREATE TABLE email_verifications (
    id CHAR(36) NOT NULL PRIMARY KEY,
    email VARCHAR(180) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at DATETIME NOT NULL,
    verified TINYINT(1) NOT NULL DEFAULT 0,
    verified_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_email_verifications_email (email),
    INDEX idx_email_verifications_email_verified (email, verified)
);
