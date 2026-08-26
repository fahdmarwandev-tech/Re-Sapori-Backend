-- liquibase formatted sql

-- changeset resapori:7-alter-menu-categories-localization
-- comment: Add English and Arabic names to menu_categories

ALTER TABLE menu_categories
    RENAME COLUMN name TO name_en;

ALTER TABLE menu_categories
    ADD COLUMN name_ar VARCHAR(255);
