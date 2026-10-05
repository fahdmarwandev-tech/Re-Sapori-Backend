-- liquibase formatted sql

-- changeset resapori:32-update-offers-and-discounts runOnChange:true
-- comment: Synchronize menu offers and item discounts with the frontend

-- 1. Synchronize original_price and discount_price
UPDATE menu_items SET original_price = 300.00, discount_price = 240.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000002';
UPDATE menu_items SET original_price = 350.00, discount_price = 250.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000000a';
UPDATE menu_items SET original_price = 350.00, discount_price = 280.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000008';
UPDATE menu_items SET original_price = 350.00, discount_price = 200.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000016';
UPDATE menu_items SET original_price = 500.00, discount_price = 400.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000000d';
UPDATE menu_items SET original_price = 400.00, discount_price = 320.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000003';
UPDATE menu_items SET original_price = 500.00, discount_price = 400.00, updated_at = NOW() WHERE id = 'a30dfd0d-c89b-418d-bc5b-18b8b9fcf656';
UPDATE menu_items SET original_price = 200.00, discount_price = 180.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000011';
UPDATE menu_items SET original_price = 300.00, discount_price = 230.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000000f';
UPDATE menu_items SET original_price = 450.00, discount_price = 360.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000004';
UPDATE menu_items SET original_price = 450.00, discount_price = 360.00, updated_at = NOW() WHERE id = 'a6c42daf-b0d9-44ea-ab21-660191f27d55';
UPDATE menu_items SET original_price = 450.00, discount_price = 360.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000000c';
UPDATE menu_items SET original_price = 70.00, discount_price = 50.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000014';
UPDATE menu_items SET original_price = 480.00, discount_price = 380.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000001';
UPDATE menu_items SET original_price = 150.00, discount_price = 120.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000013';
UPDATE menu_items SET original_price = 650.00, discount_price = 500.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000009';
UPDATE menu_items SET original_price = 650.00, discount_price = 520.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000005';
UPDATE menu_items SET original_price = 350.00, discount_price = 280.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000000b';
UPDATE menu_items SET original_price = 280.00, discount_price = 220.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000010';
UPDATE menu_items SET original_price = 450.00, discount_price = 360.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000006';
UPDATE menu_items SET original_price = 320.00, discount_price = 230.00, updated_at = NOW() WHERE id = 'c317ff40-a2a3-42fb-a8a3-d88b42f676c0';
UPDATE menu_items SET original_price = 600.00, discount_price = 480.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000007';

-- 2. Seed sample active promo code SAPORI10 (10% discount)
INSERT INTO promo_codes (
    id, code, description_en, description_ar, discount_type,
    discount_value, free_item_id, user_id, expiry_date, max_uses, current_uses, max_uses_per_user,
    is_active, created_at, updated_at
)
VALUES (
    'f0000000-0000-0000-0000-000000000001',
    'SAPORI10',
    '10% discount on entire order',
    'خصم 10% على إجمالي الطلب',
    'PERCENTAGE',
    10.00,
    NULL,
    NULL,
    NULL,
    NULL,
    0,
    1,
    TRUE,
    NOW(),
    NOW()
)
ON CONFLICT (code) DO NOTHING;
