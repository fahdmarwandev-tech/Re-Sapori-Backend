-- liquibase formatted sql

-- changeset resapori:47-add-category-addons-and-make-combo
-- comment: Support category-level menu_addons and seed "Make it a Combo" (+50.00 EGP) for all categories except Sauce, Desserts, and Drinks

ALTER TABLE menu_addons
    ALTER COLUMN menu_item_id DROP NOT NULL;

ALTER TABLE menu_addons
    ADD COLUMN IF NOT EXISTS category_id UUID;

ALTER TABLE menu_addons
    ADD CONSTRAINT fk_menu_addons_category
        FOREIGN KEY (category_id)
        REFERENCES menu_categories (id)
        ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_menu_addons_category_id ON menu_addons(category_id);

-- Seed fixed "Make it a Combo" (+50.00 EGP) option on every active category except Sauce, Desserts, and Drinks
INSERT INTO menu_addons (id, menu_item_id, category_id, name_en, name_ar, price, is_active, created_at, updated_at)
SELECT
    gen_random_uuid(),
    NULL,
    c.id,
    'Make it a Combo',
    'خليها كومبو',
    50.00,
    TRUE,
    NOW(),
    NOW()
FROM menu_categories c
WHERE c.is_active = TRUE
  AND LOWER(TRIM(c.name_en)) NOT LIKE '%sauc%'
  AND LOWER(TRIM(c.name_en)) NOT LIKE '%dip%'
  AND LOWER(TRIM(c.name_en)) NOT LIKE '%dessert%'
  AND LOWER(TRIM(c.name_en)) NOT LIKE '%sweet%'
  AND LOWER(TRIM(c.name_en)) NOT LIKE '%drink%'
  AND LOWER(TRIM(c.name_en)) NOT LIKE '%beverag%'
  AND NOT EXISTS (
      SELECT 1 FROM menu_addons a
      WHERE a.category_id = c.id
        AND LOWER(TRIM(a.name_en)) = 'make it a combo'
        AND a.is_active = TRUE
  );
