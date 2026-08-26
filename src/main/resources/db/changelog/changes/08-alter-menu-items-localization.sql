-- liquibase formatted sql

-- changeset resapori:8-alter-menu-items-localization
-- comment: Add English and Arabic names and descriptions to menu_items

ALTER TABLE menu_items
    RENAME COLUMN name TO name_en;

ALTER TABLE menu_items
    RENAME COLUMN description TO description_en;

ALTER TABLE menu_items
    ADD COLUMN name_ar VARCHAR(255),
    ADD COLUMN description_ar TEXT;
