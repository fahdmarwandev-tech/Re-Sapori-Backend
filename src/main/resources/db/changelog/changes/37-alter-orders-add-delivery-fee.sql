-- liquibase formatted sql

-- changeset resapori:37-alter-orders-add-delivery-fee
-- comment: Add delivery_fee column to orders table

ALTER TABLE orders
    ADD COLUMN delivery_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
