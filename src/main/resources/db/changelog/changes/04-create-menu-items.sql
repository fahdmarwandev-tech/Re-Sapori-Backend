-- liquibase formatted sql

-- changeset resapori:4-create-menu-items
-- comment: Create the menu_items table with FK to menu_categories and audit columns

CREATE TABLE menu_items (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id     UUID            NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    current_price   DECIMAL(10, 2)  NOT NULL,
    image_url       VARCHAR(512),
    is_available    BOOLEAN         NOT NULL DEFAULT TRUE,
    stock           INT,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(255),

    CONSTRAINT fk_menu_items_category
        FOREIGN KEY (category_id)
        REFERENCES menu_categories (id)
        ON DELETE RESTRICT
);
