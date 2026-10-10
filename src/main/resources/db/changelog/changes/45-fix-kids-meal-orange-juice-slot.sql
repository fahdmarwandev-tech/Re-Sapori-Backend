-- liquibase formatted sql

-- changeset resapori:45-fix-kids-meal-orange-juice-slot runOnChange:true
-- comment: Set Kids Meal drink slot to fixed Orange Juice instead of generic user choice

-- 1. Update slot 'a0000000-0000-0000-0000-000000000004' to fixed Orange Juice
UPDATE offer_slots
SET slot_name_en = 'Orange Juice',
    slot_name_ar = 'عصير برتقال',
    menu_item_id = 'd0000000-0000-0000-0000-000000000017',
    category_id = 'c0000000-0000-0000-0000-000000000006',
    is_free = TRUE,
    updated_at = NOW()
WHERE id = 'a0000000-0000-0000-0000-000000000004';

-- 2. Ensure Orange Juice is in eligible items for this slot
INSERT INTO offer_slot_eligible_items (slot_id, menu_item_id)
VALUES ('a0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000017')
ON CONFLICT DO NOTHING;
