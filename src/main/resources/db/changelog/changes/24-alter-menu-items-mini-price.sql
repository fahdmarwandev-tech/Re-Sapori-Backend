-- liquibase formatted sql

-- changeset resapori:24-alter-menu-items-mini-price
-- comment: Add mini_price column to menu_items for items that have a smaller portion price

ALTER TABLE menu_items
    ADD COLUMN mini_price DECIMAL(10, 2);
