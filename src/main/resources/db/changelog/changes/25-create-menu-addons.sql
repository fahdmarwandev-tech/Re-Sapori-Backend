-- liquibase formatted sql

-- changeset resapori:25-create-menu-addons
-- comment: Create menu_addons table for per-item optional add-ons

CREATE TABLE menu_addons (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    menu_item_id    UUID            NOT NULL,
    name_en         VARCHAR(255)    NOT NULL,
    name_ar         VARCHAR(255),
    price           DECIMAL(10, 2)  NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(255),

    CONSTRAINT fk_menu_addons_item
        FOREIGN KEY (menu_item_id)
        REFERENCES menu_items (id)
        ON DELETE CASCADE
);
