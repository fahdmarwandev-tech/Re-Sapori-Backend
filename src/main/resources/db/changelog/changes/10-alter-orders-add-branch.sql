-- liquibase formatted sql

-- changeset resapori:10-alter-orders-add-branch
-- comment: Add branch_id to orders

ALTER TABLE orders
    ADD COLUMN branch_id UUID;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_branch
    FOREIGN KEY (branch_id)
    REFERENCES branches (id)
    ON DELETE RESTRICT;
