-- liquibase formatted sql

-- changeset resapori:3-create-menu-categories
-- comment: Create the menu_categories table with audit columns

CREATE TABLE menu_categories (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255)    NOT NULL,
    display_order   INT,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(255)
);
