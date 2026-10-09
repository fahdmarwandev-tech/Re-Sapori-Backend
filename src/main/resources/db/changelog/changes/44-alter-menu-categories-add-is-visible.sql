-- liquibase formatted sql

-- changeset resapori:44-alter-menu-categories-add-is-visible
-- comment: Add is_visible column to menu_categories for storefront toggling without deleting

ALTER TABLE menu_categories
    ADD COLUMN IF NOT EXISTS is_visible BOOLEAN NOT NULL DEFAULT TRUE;
