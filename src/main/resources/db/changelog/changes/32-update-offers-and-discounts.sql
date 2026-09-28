-- liquibase formatted sql

-- changeset resapori:32-update-offers-and-discounts runOnChange:true
-- comment: Synchronize menu offers and item discounts with the frontend

-- 1. Apply discount to Margherita pizza (300 -> 240 EGP, 20% discount)
UPDATE menu_items
SET current_price  = 240.00,
    original_price = 300.00,
    discount_price = 240.00,
    updated_at     = NOW()
WHERE name_en = 'Margherita'
   OR id = 'd0000000-0000-0000-0000-000000000002';

-- 2. Deactivate obsolete offer combos not present in frontend (Box, Double Deal, Group Meal, Double Trip)
UPDATE menu_items
SET is_active    = FALSE,
    is_available = FALSE,
    updated_at   = NOW()
WHERE name_en IN ('Box', 'Double Deal', 'Group Meal', 'Double Trip')
   OR id IN (
      'd0000000-0000-0000-0000-00000000001d',
      'd0000000-0000-0000-0000-000000000020',
      'd0000000-0000-0000-0000-000000000021',
      'd0000000-0000-0000-0000-000000000022'
   );

-- 3. Update Kids Meal to match frontend specs
UPDATE menu_items
SET name_en        = 'Kids Meal',
    name_ar        = 'وجبة الأطفال',
    description_en = 'Small Pizza (Margherita or Chicken Pesto) + packet fries + orange juice + toy.',
    description_ar = 'بيتزا صغيرة (مارجريتا او تشيكن بيستو) - باكت بطاطس - عصير برتقال - لعبة هدية',
    current_price  = 250.00,
    image_url      = '/assets/menu_images/kidsmeal.png',
    is_available   = TRUE,
    is_active      = TRUE,
    updated_at     = NOW()
WHERE name_en = 'Kids Meal'
   OR id = 'd0000000-0000-0000-0000-00000000001c';

-- 4. Upsert Single Meal (1 Pizza + 1 Salad with 10% discount)
INSERT INTO menu_items (
    id, category_id, name_en, name_ar, description_en, description_ar,
    current_price, original_price, discount_price, image_url, is_available, is_active, created_at, updated_at
)
VALUES (
    'd0000000-0000-0000-0000-000000000028',
    'c0000000-0000-0000-0000-000000000007',
    'Single Meal',
    'وجبة فردية',
    '1 Pizza of your choice - 1 Salad of your choice.',
    '١ بيتزا من اختيارك - ١ سلطة من اختيارك',
    450.00,
    500.00,
    450.00,
    '/assets/menu_images/singlemeal.png',
    TRUE,
    TRUE,
    NOW(),
    NOW()
)
ON CONFLICT (id) DO UPDATE SET
    category_id    = EXCLUDED.category_id,
    name_en        = EXCLUDED.name_en,
    name_ar        = EXCLUDED.name_ar,
    description_en = EXCLUDED.description_en,
    description_ar = EXCLUDED.description_ar,
    current_price  = EXCLUDED.current_price,
    original_price = EXCLUDED.original_price,
    discount_price = EXCLUDED.discount_price,
    image_url      = EXCLUDED.image_url,
    is_available   = EXCLUDED.is_available,
    is_active      = EXCLUDED.is_active,
    updated_at     = NOW();

-- 5. Update Family Meal to match frontend specs
UPDATE menu_items
SET name_en        = 'Family Meal',
    name_ar        = 'وجبة العائلة',
    description_en = '3 Pizzas of your choice - 2 Sauces of your choice - 1 Free Pizza - 1 Liter (Mix Cola or Juice) Free.',
    description_ar = '٣ بيتزا من اختيارك - ٢ صوص من اختيارك - ١ بيتزا هدية - ١ لتر (مكس كولا او عصير) هدية',
    current_price  = 1300.00,
    image_url      = '/assets/menu_images/familymeal.png',
    is_available   = TRUE,
    is_active      = TRUE,
    updated_at     = NOW()
WHERE name_en = 'Family Meal'
   OR id = 'd0000000-0000-0000-0000-00000000001e';

-- 6. Update Friends Meal to match frontend specs
UPDATE menu_items
SET name_en        = 'Friends Meal',
    name_ar        = 'وجبة الأصدقاء',
    description_en = '2 Pizzas of your choice - 1 Small Pizza Free - 1 Liter (Mix Cola or Juice) Free',
    description_ar = '٢ بيتزا من اختيارك - ١ بيتزا صغير هدية - ١ لتر (مكس كولا او عصير) هدية',
    current_price  = 600.00,
    image_url      = '/assets/menu_images/friendsmeal.png',
    is_available   = TRUE,
    is_active      = TRUE,
    updated_at     = NOW()
WHERE name_en = 'Friends Meal'
   OR id = 'd0000000-0000-0000-0000-00000000001f';

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
