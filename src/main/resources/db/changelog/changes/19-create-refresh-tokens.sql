-- liquibase formatted sql

-- changeset resapori:19-create-refresh-tokens
-- comment: Create the refresh_tokens table for JWT auth

CREATE TABLE refresh_tokens (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL,
    token           VARCHAR(255)    NOT NULL UNIQUE,
    expiry_date     TIMESTAMP       NOT NULL,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
