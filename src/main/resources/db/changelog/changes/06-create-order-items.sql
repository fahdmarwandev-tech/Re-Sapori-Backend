-- liquibase formatted sql

-- changeset resapori:6-create-order-items
-- comment: Create the order_items table with FKs to orders and menu_items, plus audit columns

CREATE TABLE order_items (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID            NOT NULL,
    menu_item_id            UUID            NOT NULL,
    quantity                INT             NOT NULL,
    unit_price_at_purchase  DECIMAL(10, 2)  NOT NULL,
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(255),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              VARCHAR(255),

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_items_menu_item
        FOREIGN KEY (menu_item_id)
        REFERENCES menu_items (id)
        ON DELETE RESTRICT
);
