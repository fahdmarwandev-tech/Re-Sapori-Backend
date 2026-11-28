-- liquibase formatted sql

-- changeset resapori:48-guest-checkout-and-welcome-promo
-- comment: Support guest checkout (nullable user_id, customer_name column) and seed one-time WELCOME10 promo code

ALTER TABLE orders ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_name VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_orders_customer_phone_created ON orders (customer_phone, created_at);

INSERT INTO promo_codes (
    id, code, description_en, description_ar, discount_type,
    discount_value, free_item_id, user_id, expiry_date, max_uses, current_uses, max_uses_per_user,
    is_active, created_at, updated_at
)
SELECT
    '11111111-1111-4111-8111-111111111010',
    'WELCOME10',
    '10% off your first order',
    'خصم 10% على أول طلب',
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
WHERE NOT EXISTS (
    SELECT 1 FROM promo_codes WHERE UPPER(code) = 'WELCOME10'
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_welcome_promo_once_per_user
    ON promo_code_redemptions (promo_code_id, user_id)
    WHERE promo_code_id = '11111111-1111-4111-8111-111111111010';
