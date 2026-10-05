-- liquibase formatted sql

-- changeset resapori:32-update-offers-and-discounts runOnChange:true
-- comment: Synchronize menu offers and item discounts with the frontend

-- Menu items and discounts are now comprehensively seeded and maintained in 28-seed-menu-data.sql

-- 7. Seed sample active promo code SAPORI10 (10% discount)
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
