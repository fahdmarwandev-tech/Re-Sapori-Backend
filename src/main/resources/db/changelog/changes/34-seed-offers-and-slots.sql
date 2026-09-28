-- liquibase formatted sql

-- changeset resapori:34-seed-offers-and-slots runOnChange:true
-- comment: Seed active offers, offer slots, and eligible items

-- =========================================================================
-- 1. Offers
-- =========================================================================
INSERT INTO offers (
    id, name_en, name_ar, description_en, description_ar, image_url,
    discount_target, discount_percentage, fixed_price, buy_quantity, get_quantity,
    category_id, is_active, created_at, updated_at
)
VALUES
    -- Single Meal: 1 Pizza + 1 Salad with 10% discount on total
    (
        'f0000000-0000-0000-0000-000000000001',
        'Single Meal',
        'وجبة فردية',
        '1 Pizza of your choice - 1 Salad of your choice with 10% discount.',
        '١ بيتزا من اختيارك - ١ سلطة من اختيارك مع خصم ١٠٪.',
        '/assets/menu_images/singlemeal.png',
        'TOTAL_BUNDLE',
        10.00,
        NULL,
        1,
        0,
        'c0000000-0000-0000-0000-000000000007',
        TRUE,
        NOW(),
        NOW()
    ),
    -- Kids Meal: Fixed price 250 EGP bundle
    (
        'f0000000-0000-0000-0000-000000000002',
        'Kids Meal',
        'وجبة الأطفال',
        'Small Pizza (Margherita or Chicken Pesto) + packet fries + orange juice + toy.',
        'بيتزا صغيرة (مارجريتا او تشيكن بيستو) - باكت بطاطس - عصير برتقال - لعبة هدية',
        '/assets/menu_images/kidsmeal.png',
        'FIXED_PRICE',
        NULL,
        250.00,
        1,
        0,
        'c0000000-0000-0000-0000-000000000007',
        TRUE,
        NOW(),
        NOW()
    ),
    -- Friends Meal: 2 paid pizzas + 1 free small pizza + 1 free 1L drink
    -- Pricing: TOTAL_BUNDLE with 0% — customer pays full price for 2 pizzas, free items are separate slots
    (
        'f0000000-0000-0000-0000-000000000003',
        'Friends Meal',
        'وجبة الأصدقاء',
        '2 Pizzas of your choice - 1 Small Pizza Free - 1 Liter (Mix Cola or Juice) Free.',
        '٢ بيتزا من اختيارك - ١ بيتزا صغير هدية - ١ لتر (مكس كولا او عصير) هدية.',
        '/assets/menu_images/friendsmeal.png',
        'TOTAL_BUNDLE',
        0.00,
        NULL,
        2,
        1,
        'c0000000-0000-0000-0000-000000000007',
        TRUE,
        NOW(),
        NOW()
    ),
    -- Family Meal: 4 pizzas (cheapest free) + 2 paid sauces + 1 free 1L drink
    -- Pricing: CHEAPEST_ITEM with 100% discount — cheapest of 4 pizzas is auto-free
    (
        'f0000000-0000-0000-0000-000000000004',
        'Family Meal',
        'وجبة العائلة',
        '3 Pizzas of your choice - 2 Sauces of your choice - 1 Free Pizza - 1 Liter (Mix Cola or Juice) Free.',
        '٣ بيتزا من اختيارك - ٢ صوص من اختيارك - ١ بيتزا هدية - ١ لتر (مكس كولا او عصير) هدية.',
        '/assets/menu_images/familymeal.png',
        'CHEAPEST_ITEM',
        100.00,
        NULL,
        3,
        1,
        'c0000000-0000-0000-0000-000000000007',
        TRUE,
        NOW(),
        NOW()
    )
ON CONFLICT (id) DO UPDATE SET
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    description_en = EXCLUDED.description_en,
    description_ar = EXCLUDED.description_ar,
    image_url = EXCLUDED.image_url,
    discount_target = EXCLUDED.discount_target,
    discount_percentage = EXCLUDED.discount_percentage,
    fixed_price = EXCLUDED.fixed_price,
    buy_quantity = EXCLUDED.buy_quantity,
    get_quantity = EXCLUDED.get_quantity,
    category_id = EXCLUDED.category_id,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- =========================================================================
-- 2. Offer Slots
-- =========================================================================

-- ── Single Meal: 1 Pizza (paid) + 1 Salad (paid) ──
INSERT INTO offer_slots (
    id, offer_id, slot_name_en, slot_name_ar, category_id, quantity, display_order, is_free, is_active, created_at, updated_at
)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001', 'Choose Your Pizza', 'اختر نوع البيتزا', 'c0000000-0000-0000-0000-000000000001', 1, 1, FALSE, TRUE, NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000001', 'Choose Your Salad', 'اختر نوع السلطة', 'c0000000-0000-0000-0000-000000000003', 1, 2, FALSE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    slot_name_en = EXCLUDED.slot_name_en,
    slot_name_ar = EXCLUDED.slot_name_ar,
    category_id = EXCLUDED.category_id,
    quantity = EXCLUDED.quantity,
    display_order = EXCLUDED.display_order,
    is_free = EXCLUDED.is_free,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- ── Kids Meal: 1 Pizza (specific: Margherita or Chicken Pesto, paid) + 1 Drink (free) ──
INSERT INTO offer_slots (
    id, offer_id, slot_name_en, slot_name_ar, category_id, quantity, display_order, is_free, is_active, created_at, updated_at
)
VALUES
    ('a0000000-0000-0000-0000-000000000003', 'f0000000-0000-0000-0000-000000000002', 'Choose Your Pizza', 'اختر نوع البيتزا', 'c0000000-0000-0000-0000-000000000001', 1, 1, FALSE, TRUE, NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000004', 'f0000000-0000-0000-0000-000000000002', 'Choose Drink', 'اختر المشروب', 'c0000000-0000-0000-0000-000000000006', 1, 2, TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    slot_name_en = EXCLUDED.slot_name_en,
    slot_name_ar = EXCLUDED.slot_name_ar,
    category_id = EXCLUDED.category_id,
    quantity = EXCLUDED.quantity,
    display_order = EXCLUDED.display_order,
    is_free = EXCLUDED.is_free,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- Kids Meal Pizza slot: only Margherita & Chicken Pesto allowed
INSERT INTO offer_slot_eligible_items (slot_id, menu_item_id)
VALUES
    ('a0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000002'),
    ('a0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001')
ON CONFLICT DO NOTHING;

-- ── Friends Meal: 2 Pizzas (paid) + 1 Free Small Pizza + 1 Free 1L Drink (restricted) ──
INSERT INTO offer_slots (
    id, offer_id, slot_name_en, slot_name_ar, category_id, quantity, display_order, is_free, is_active, created_at, updated_at
)
VALUES
    ('a0000000-0000-0000-0000-000000000005', 'f0000000-0000-0000-0000-000000000003', 'Choose 2 Pizzas', 'اختر ٢ بيتزا', 'c0000000-0000-0000-0000-000000000001', 2, 1, FALSE, TRUE, NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000006', 'f0000000-0000-0000-0000-000000000003', 'Free Small Pizza', 'بيتزا صغيرة هدية', 'c0000000-0000-0000-0000-000000000001', 1, 2, TRUE, TRUE, NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000007', 'f0000000-0000-0000-0000-000000000003', 'Free 1L Drink', 'مشروب ١ لتر هدية', 'c0000000-0000-0000-0000-000000000006', 1, 3, TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    slot_name_en = EXCLUDED.slot_name_en,
    slot_name_ar = EXCLUDED.slot_name_ar,
    category_id = EXCLUDED.category_id,
    quantity = EXCLUDED.quantity,
    display_order = EXCLUDED.display_order,
    is_free = EXCLUDED.is_free,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- Friends Meal drink slot: only Mix Cola & Fresh Juice allowed
INSERT INTO offer_slot_eligible_items (slot_id, menu_item_id)
VALUES
    ('a0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000018'),
    ('a0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000017')
ON CONFLICT DO NOTHING;

-- ── Family Meal: 4 Pizzas (cheapest auto-free) + 2 Sauces (paid) + 1 Free 1L Drink (restricted) ──
-- NOTE: Customer picks 4 pizzas in 1 slot. The CHEAPEST_ITEM engine (buy=3, get=1, 100%)
--       automatically makes the cheapest pizza free. Sauces are PAID (is_free=FALSE).
INSERT INTO offer_slots (
    id, offer_id, slot_name_en, slot_name_ar, category_id, quantity, display_order, is_free, is_active, created_at, updated_at
)
VALUES
    -- Slot 1: 4 Pizzas in one slot (cheapest will be auto-free by pricing engine)
    ('a0000000-0000-0000-0000-000000000008', 'f0000000-0000-0000-0000-000000000004', 'Choose 4 Pizzas', 'اختر ٤ بيتزا', 'c0000000-0000-0000-0000-000000000001', 4, 1, FALSE, TRUE, NOW(), NOW()),
    -- Slot 2: 2 Sauces — PAID (is_free = FALSE)
    ('a0000000-0000-0000-0000-00000000000a', 'f0000000-0000-0000-0000-000000000004', 'Choose 2 Sauces', 'اختر ٢ صوص', 'c0000000-0000-0000-0000-000000000008', 2, 2, FALSE, TRUE, NOW(), NOW()),
    -- Slot 3: 1 Free 1L Drink (restricted to Cola & Juice)
    ('a0000000-0000-0000-0000-00000000000b', 'f0000000-0000-0000-0000-000000000004', 'Free 1L Drink', 'مشروب ١ لتر هدية', 'c0000000-0000-0000-0000-000000000006', 1, 3, TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    slot_name_en = EXCLUDED.slot_name_en,
    slot_name_ar = EXCLUDED.slot_name_ar,
    category_id = EXCLUDED.category_id,
    quantity = EXCLUDED.quantity,
    display_order = EXCLUDED.display_order,
    is_free = EXCLUDED.is_free,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- Delete the old separate "Free Pizza" slot (a0000000-...-000000000009) that is no longer needed
-- (Family Meal now uses a single 4-pizza slot instead of 3+1 separate slots)
DELETE FROM offer_slots WHERE id = 'a0000000-0000-0000-0000-000000000009';

-- Family Meal drink slot: only Mix Cola & Fresh Juice allowed
INSERT INTO offer_slot_eligible_items (slot_id, menu_item_id)
VALUES
    ('a0000000-0000-0000-0000-00000000000b', 'd0000000-0000-0000-0000-000000000018'),
    ('a0000000-0000-0000-0000-00000000000b', 'd0000000-0000-0000-0000-000000000017')
ON CONFLICT DO NOTHING;
