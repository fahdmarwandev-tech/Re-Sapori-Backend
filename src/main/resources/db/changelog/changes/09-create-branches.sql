-- liquibase formatted sql

-- changeset resapori:9-create-branches
-- comment: Create branches table

CREATE TABLE branches (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255)    NOT NULL,
    address         TEXT,
    phone_number    VARCHAR(20),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(255)
);
