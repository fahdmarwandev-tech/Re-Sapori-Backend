-- liquibase formatted sql

-- changeset resapori:14a-extend-order-status-enum runInTransaction:false
-- comment: Extend the order_status PostgreSQL enum with payment lifecycle values.
--          Must run outside a transaction (runInTransaction:false) because PostgreSQL
--          does not allow ALTER TYPE ADD VALUE inside a transaction block.

ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'PAYMENT_PENDING';
ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'PAYMENT_FAILED';

-- changeset resapori:14b-alter-orders-payment-fields
-- comment: Add order_type, payment_method, currency, and Paymob tracking columns to orders.
--          order_type is nullable to avoid locking or failing on existing rows.

ALTER TABLE orders
    ADD COLUMN order_type            order_type     NULL,
    ADD COLUMN payment_method        payment_method NULL,
    ADD COLUMN currency              VARCHAR(3)     NOT NULL DEFAULT 'EGP',
    ADD COLUMN paymob_order_id       VARCHAR(255)   NULL,
    ADD COLUMN paymob_transaction_id VARCHAR(255)   NULL;
