-- liquibase formatted sql

-- changeset resapori:21-create-user-addresses
-- comment: Create user_addresses table and drop the old single address column from users

CREATE TABLE user_addresses (
    id            UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID            NOT NULL,
    label         VARCHAR(100),
    street        VARCHAR(255)    NOT NULL,
    city          VARCHAR(100)    NOT NULL,
    district      VARCHAR(100),
    floor         VARCHAR(20),
    apartment     VARCHAR(20),
    lat           DECIMAL(10, 7),
    lng           DECIMAL(10, 7),
    is_default    BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(255),
    updated_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by    VARCHAR(255),

    CONSTRAINT fk_user_addresses_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

ALTER TABLE users DROP COLUMN IF EXISTS address;
