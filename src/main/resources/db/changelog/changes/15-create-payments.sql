-- liquibase formatted sql

-- changeset resapori:15-create-payments
-- comment: Create the payments table to store each Paymob transaction attempt per order.
--          Multiple payment rows per order are allowed (e.g., one failed + one successful attempt).

CREATE TABLE payments (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID            NOT NULL,
    paymob_order_id         VARCHAR(255),
    paymob_transaction_id   VARCHAR(255)    UNIQUE,
    status                  payment_status  NOT NULL DEFAULT 'PENDING',
    payment_method          payment_method,
    amount_cents            BIGINT          NOT NULL,
    currency                VARCHAR(3)      NOT NULL DEFAULT 'EGP',
    paymob_callback_raw     TEXT,
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(255),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              VARCHAR(255),

    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_payments_order_id             ON payments (order_id);
CREATE INDEX idx_payments_paymob_transaction_id ON payments (paymob_transaction_id);
