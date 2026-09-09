-- liquibase formatted sql

-- changeset resapori:23-alter-menu-categories-subtitle
-- comment: Add subtitle_en and subtitle_ar to menu_categories

ALTER TABLE menu_categories
    ADD COLUMN subtitle_en TEXT,
    ADD COLUMN subtitle_ar TEXT;
