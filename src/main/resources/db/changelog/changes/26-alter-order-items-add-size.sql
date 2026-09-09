-- liquibase formatted sql

-- changeset resapori:26-alter-order-items-add-size
-- comment: Add size column to order_items to track REGULAR vs MINI portion

ALTER TABLE order_items
    ADD COLUMN size VARCHAR(20) NOT NULL DEFAULT 'REGULAR';
