-- liquibase formatted sql

-- changeset resapori:31-alter-menu-items-add-discount-and-original-price
-- comment: Add discount_price and original_price columns to menu_items to support item discounts

ALTER TABLE menu_items
    ADD COLUMN discount_price DECIMAL(10, 2),
    ADD COLUMN original_price DECIMAL(10, 2);
