-- liquibase formatted sql

-- changeset resapori:5-create-orders
-- comment: Create the orders table with FK to users and audit columns

CREATE TABLE orders (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL,
    status              order_status    NOT NULL DEFAULT 'pending',
    total_amount        DECIMAL(10, 2)  NOT NULL,
    delivery_address    TEXT,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(255),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          VARCHAR(255),

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE RESTRICT
);
