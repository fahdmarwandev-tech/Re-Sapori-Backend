-- liquibase formatted sql

-- changeset resapori:28-seed-menu-data runOnChange:true
-- comment: Seed initial menu categories, items, and add-ons idempotently matching frontend menu
-- validCheckSum: ANY

-- =========================================================================
-- 1. Categories (8 categories)
-- =========================================================================
INSERT INTO menu_categories (id, name_en, name_ar, display_order, subtitle_en, subtitle_ar, is_active, created_at, updated_at)
VALUES
    ('c0000000-0000-0000-0000-000000000001', 'Pizza', 'بيتزا', 1, 'Authentic Italian pizza, crafted with carefully selected ingredients.', 'بيتزا إيطالية أصيلة، مصنوعة بمكونات مختارة بعناية.', TRUE, NOW(), NOW()),
    ('c0000000-0000-0000-0000-000000000002', 'Pasta', 'باستا', 2, 'Handcrafted pasta, inspired by the heart of Italy.', 'باستا مصنوعة يدوياً، مستوحاة من قلب إيطاليا.', TRUE, NOW(), NOW()),
    ('c0000000-0000-0000-0000-000000000003', 'Salad', 'سلطة', 3, 'Fresh, bright, and simple Italian-style starters.', 'مقبلات طازجة وخفيفة وبسيطة على الطريقة الإيطالية.', TRUE, NOW(), NOW()),
    ('c0000000-0000-0000-0000-000000000004', 'Appetizers', 'مقبلات', 4, 'Perfect bites to start your meal.', 'لقيمات مثالية لبدء وجبتك.', TRUE, NOW(), NOW()),
    ('c0000000-0000-0000-0000-000000000005', 'Desserts', 'حلويات', 5, 'A sweet finish to your Italian table.', 'نهاية حلوة لمائدتك الإيطالية.', TRUE, NOW(), NOW()),
    ('c0000000-0000-0000-0000-000000000006', 'Drinks', 'مشروبات', 6, 'Refreshing pairings for every dish.', 'مشروبات منعشة ترافق كل طبق.', TRUE, NOW(), NOW()),
    ('c0000000-0000-0000-0000-000000000007', 'Offers', 'عروض', 7, 'Special combinations and seasonal savings.', 'مجموعات خاصة وتوفير موسمي.', TRUE, NOW(), NOW()),
    ('c0000000-0000-0000-0000-000000000008', 'Sauce', 'صوص', 8, 'Complement your dishes with signature dipping sauces.', 'أكمل أطباقك بصلصات التغميس المميزة.', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    display_order = EXCLUDED.display_order,
    subtitle_en = EXCLUDED.subtitle_en,
    subtitle_ar = EXCLUDED.subtitle_ar,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- =========================================================================
-- Deactivate obsolete combo items not present in target menu
-- =========================================================================
UPDATE menu_items
SET is_active = FALSE,
    is_available = FALSE,
    updated_at = NOW()
WHERE id IN (
    'd0000000-0000-0000-0000-00000000001d', -- Box
    'd0000000-0000-0000-0000-000000000020', -- Double Deal
    'd0000000-0000-0000-0000-000000000021', -- Group Meal
    'd0000000-0000-0000-0000-000000000022'  -- Double Trip
);

-- =========================================================================
-- Category: Pizza (بيتزا) - 9 items
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'Margherita', 'مارجريتا', 'Tomato sauce + basil + mozzarella + parmesan.', 'صلصة طماطم + ريحان + موتزاريلا + بارميزان.', 300.00, NULL, '/uploads/menu-items/44df7ac6-6e7e-4810-ba86-fbaad3c73263.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001', 'Vegetarian', 'خضروات', 'Tomato sauce + cherry tomatoes + sweet corn + basil + mozzarella + parmesan.', 'صلصة طماطم + طماطم شيري + ذرة حلوة + ريحان + موتزاريلا + بارميزان.', 400.00, NULL, '/uploads/menu-items/8a3a2f01-4771-47dd-98f8-c0044edcd3a5.png', TRUE, TRUE, NOW(), NOW()),
    ('a30dfd0d-c89b-418d-bc5b-18b8b9fcf656', 'c0000000-0000-0000-0000-000000000001', 'Chicken ranch', 'تشيكن رانش', 'Ranch + white sauce + chicken + bazil+ mozzarella + parmesan.', 'رانش + صوص ابيض + دجاج + ريحان + موزاريلا + بارميزان', 500.00, NULL, '/uploads/menu-items/48b96c03-e625-42a4-84ab-79db62f446f8.webp', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000001', 'Pepperoni/Diavola', 'بيبروني', 'Tomato sauce + jalapeño + salami + bazil + mozzarella + parmesan.', 'صلصة طماطم + هلابينو + سلامي + ريحان + موتزاريلا + بارميزان.', 450.00, NULL, '/uploads/menu-items/801110a1-b90a-4d02-a26d-b13f112c16f4.png', TRUE, TRUE, NOW(), NOW()),
    ('a6c42daf-b0d9-44ea-ab21-660191f27d55', 'c0000000-0000-0000-0000-000000000001', 'Burratta', 'بوراتا', 'Tomato sauce + bazil + burrata + baby rocca + parmesan', 'صلصة طماطم - ريحان - بوراتا - بيبي روكا - بارميزان', 450.00, NULL, '/uploads/menu-items/85426234-dc39-4dec-9602-a636e0908782.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'Chicken Pesto', 'دجاج بيستو', 'Pesto + white sauce + chicken + pesto sauce + mozzarella + parmesan.', 'صلصة بيستو + صلصة بيضاء + دجاج + بيستو + موتزاريلا + بارميزان.', 480.00, NULL, '/uploads/menu-items/85f1d93a-06b4-42d9-b2a6-1a51b5207769.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000001', 'Creamy Salmon', 'كريمي سالمون', 'White sauce + salmon + shrimp + Kiri + basil + mozzarella + parmesan.', 'صلصة بيضاء + سالمون + جمبري + كيري + ريحان + موتزاريلا + بارميزان.', 650.00, NULL, '/uploads/menu-items/7b1aff49-9f87-42e4-b41d-9a00e27b16d2.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000001', 'Quattro', 'كواترو', 'White sauce + blue cheese + Gouda cheese + red cheddar + honey + basil + mozzarella + parmesan.', 'صلصة بيضاء + جبنة ريكفورد + جبنة جودة + تشيدر أحمر + عسل + ريحان + موتزاريلا + بارميزان.', 450.00, NULL, '/uploads/menu-items/735ded20-874c-4252-9a4a-c9442113d66b.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000001', 'Frutti di Mare', 'فروتي دي ماري', 'White sauce + shrimp + calamari + sea mussels + mozzarella + parmesan.', 'صلصة بيضاء + جمبري + كالماري + بلح البحر + موتزاريلا + بارميزان.', 600.00, NULL, '/uploads/menu-items/81bde2e9-7209-44f1-9e50-f066de004050.png', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    description_en = EXCLUDED.description_en,
    description_ar = EXCLUDED.description_ar,
    current_price = EXCLUDED.current_price,
    mini_price = EXCLUDED.mini_price,
    image_url = EXCLUDED.image_url,
    is_available = EXCLUDED.is_available,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- =========================================================================
-- Category: Pasta (باستا) - 7 items
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-00000000000a', 'c0000000-0000-0000-0000-000000000002', 'Alfredo', 'ألفريدو', 'Fettuccine + white sauce + chicken + mushroom + parmesan.', 'فيتوتشيني + صوص أبيض + دجاج + مشروم + جبنة بارميزان.', 350.00, NULL, '/uploads/menu-items/3ea7efad-d790-4e0a-a0eb-bc12cb933c55.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000002', 'Pesto', 'بيستو', 'Penne + white sauce + pesto sauce + pesto + parmesan.', 'مكرونة + صلصة بيضاء + صلصة بيستو + بيستو + بارميزان.', 350.00, NULL, '/uploads/menu-items/cf20a26a-21fd-40ad-936c-61ce688f6a2a.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000000d', 'c0000000-0000-0000-0000-000000000002', 'Re Sapori', 'ري سابوري', 'Penne + shrimp + chicken + mushroom + white sauce + onion + garlic + pesto + parmesan.', 'بيني + جمبري + دجاج + مشروم + صلصة بيضاء + بصل + ثوم + بيستو + بارميزان.', 500.00, NULL, '/uploads/menu-items/e8e4e59f-6488-4705-bb4a-96eb9a5e9285.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000000c', 'c0000000-0000-0000-0000-000000000002', 'Signature', 'توقيعنا (سيجنتشر)', 'Penne + pepperoni + chicken + red cheddar + Gouda cheese + white sauce + pesto + parmesan.', 'بيني + بيبروني + دجاج + تشيدر أحمر + جبنة جودة + صلصة بيضاء + بيستو + بارميزان.', 450.00, NULL, '/uploads/menu-items/976e778a-2df7-4340-ba96-24e61ea17cbf.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000009', 'c0000000-0000-0000-0000-000000000002', 'Creamy Salmon', 'كريمي سالمون', 'Penne + tomato sauce + fresh cream + capers + salmon + pesto + parmesan.', 'مكرونة + صلصة طماطم + كريمة طازجة + كابري + سالمون + بيستو + بارميزان.', 650.00, NULL, '/uploads/menu-items/26b0ef21-6466-44c7-b18b-0cac48617f2a.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000000b', 'c0000000-0000-0000-0000-000000000002', 'Bolognese', 'بولونيز', 'Spaghetti + tomato sauce + bolognese + cherry tomatoes + pesto + parmesan.', 'سباجيتي + صلصة طماطم + بولونيز + طماطم شيري + بيستو + بارميزان.', 350.00, NULL, '/uploads/menu-items/4c87af3e-9c5c-4d3c-84de-76327417cf4e.png', TRUE, TRUE, NOW(), NOW()),
    ('c317ff40-a2a3-42fb-a8a3-d88b42f676c0', 'c0000000-0000-0000-0000-000000000002', 'Arabiata', 'ارابياتا', 'Spaghetti + Red sauce + Cherry tomato + Bazel + Parmesan', 'سباجتي + صوص طماطم + طماطم شيري + ريحان + بارميزان', 320.00, NULL, '/uploads/menu-items/34a67d9b-914a-4cb7-a682-75d1e3e1290e.webp', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    description_en = EXCLUDED.description_en,
    description_ar = EXCLUDED.description_ar,
    current_price = EXCLUDED.current_price,
    mini_price = EXCLUDED.mini_price,
    image_url = EXCLUDED.image_url,
    is_available = EXCLUDED.is_available,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- =========================================================================
-- Category: Salad (سلطة) - 4 items
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000003', 'Rocca', 'روكا', 'Cherry tomatoes + mushroom + lemon dressing + parmesan + watercress.', 'طماطم شيري + مشروم + صوص الليمون + بارميزان + جرجير.', 200.00, NULL, '/uploads/menu-items/ea3497d4-f805-432e-9288-de624666a776.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000000f', 'c0000000-0000-0000-0000-000000000003', 'Quinoa', 'كينوا', 'Batavia + quinoa + sweet corn + goat cheese + honey mustard.', 'باتافيا + كينوا + ذرة حلوة + جبنة ماعز + خردل بالعسل.', 300.00, NULL, '/uploads/menu-items/d4b55241-9cb9-4626-859e-685ba7cb80db.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000000e', 'c0000000-0000-0000-0000-000000000003', 'Caesar', 'سيزر', 'Kabocha + Caesar sauce + toast + parmesan.', 'كابوتشا + صلصة سيزر + توست + بارميزان.', 150.00, NULL, '/uploads/menu-items/924e6f01-5f83-4804-bfd1-d83335ff0302.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000010', 'c0000000-0000-0000-0000-000000000003', 'Apple Sapori', 'تفاح سابوري', 'Batavia + honey mustard + green apple + goat cheese + dried figs + watercress.', 'باتافيا + خردل بالعسل + تفاح أخضر + جبنة ماعز + تين مجفف + جرجير.', 280.00, NULL, '/uploads/menu-items/c93ee0c3-dfea-497f-8436-840f11d8adcc.png', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    description_en = EXCLUDED.description_en,
    description_ar = EXCLUDED.description_ar,
    current_price = EXCLUDED.current_price,
    mini_price = EXCLUDED.mini_price,
    image_url = EXCLUDED.image_url,
    is_available = EXCLUDED.is_available,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- =========================================================================
-- Category: Appetizers (مقبلات) - 5 items
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000015', 'c0000000-0000-0000-0000-000000000004', 'Curly Fries', 'بطاطس كيرلي', NULL, NULL, 120.00, NULL, '/uploads/menu-items/43d6bf0f-d7ee-4905-a559-fbcb5d8f1562.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000012', 'c0000000-0000-0000-0000-000000000004', 'Onion Rings', 'حلقات البصل', '5 pieces', '6 قطع', 100.00, NULL, '/uploads/menu-items/0ae82b34-29ef-420d-a2cc-54b9fe56becc.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000014', 'c0000000-0000-0000-0000-000000000004', 'French Fries', 'بطاطس مقلية', NULL, NULL, 70.00, NULL, '/uploads/menu-items/83db169c-f810-4d14-b1b9-db4461b65b34.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000013', 'c0000000-0000-0000-0000-000000000004', 'Mozzarella Sticks', 'أصابع الموتزاريلا', '5 pieces', '6 قطع', 150.00, NULL, '/uploads/menu-items/004542e0-e1af-44a2-a961-12d32c6abce9.png', TRUE, TRUE, NOW(), NOW()),
    ('14ba33b8-abfb-4871-803c-10151a22db3c', 'c0000000-0000-0000-0000-000000000004', 'Jalapeno Bites', 'هاليبينو بايتس', NULL, NULL, 90.00, NULL, '/uploads/menu-items/b40b4fad-fdd1-41d7-bfe2-3edc5e31b21a.png', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    description_en = EXCLUDED.description_en,
    description_ar = EXCLUDED.description_ar,
    current_price = EXCLUDED.current_price,
    mini_price = EXCLUDED.mini_price,
    image_url = EXCLUDED.image_url,
    is_available = EXCLUDED.is_available,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- =========================================================================
-- Category: Desserts (حلويات) - 2 items
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('e5a40487-a76d-4232-8964-a05ee364c807', 'c0000000-0000-0000-0000-000000000005', 'Panna Cotta', 'باناكوتا', 'Coconut milk + cream  + oreo.', 'لبن جوز هند + كريمة  + اوريو', 100.00, NULL, '/uploads/menu-items/d5548852-a3f2-47be-b187-5d053b683da7.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000016', 'c0000000-0000-0000-0000-000000000005', 'Calzone', 'كالزوني', 'Italian pizza dough  + powdered sugar + Nutella + chocolate + cocoa.', 'عجينة بيتزا إيطالية +  سكر بودرة + نوتيلا + شوكولاتة + كاكاو.', 350.00, NULL, '/uploads/menu-items/3c26df8a-4ee0-4f95-a663-ea71249fdd74.png', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    description_en = EXCLUDED.description_en,
    description_ar = EXCLUDED.description_ar,
    current_price = EXCLUDED.current_price,
    mini_price = EXCLUDED.mini_price,
    image_url = EXCLUDED.image_url,
    is_available = EXCLUDED.is_available,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- =========================================================================
-- Category: Drinks (مشروبات) - 9 items
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('ffa48f05-fae4-46d2-9865-7c0c81605ec2', 'c0000000-0000-0000-0000-000000000006', '1L Maxi cola/Apple', 'ا لتر ماكس كولا/تفاح', NULL, NULL, 50.00, NULL, '/uploads/menu-items/0152ce59-d9e0-4ae2-9d9b-7150e3f6645d.png', TRUE, TRUE, NOW(), NOW()),
    ('ae7e23d3-7dad-425a-a8cf-bb6c78504e8f', 'c0000000-0000-0000-0000-000000000006', '1 Liter Orange', '١ لتر عصير برتقال', NULL, NULL, 140.00, NULL, '/uploads/menu-items/ef39ada0-25c1-4b51-9f98-3bb881fdca08.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000019', 'c0000000-0000-0000-0000-000000000006', 'Red Bull', 'ريد بول', NULL, NULL, 85.00, NULL, '/uploads/menu-items/a5543f43-5fa7-4240-ba09-3190c6a2412b.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000018', 'c0000000-0000-0000-0000-000000000006', 'Can ( V Cola)', 'كانز (V cola)', NULL, NULL, 25.00, NULL, '/uploads/menu-items/8115a28c-aaf3-4d00-b7da-925921da5467.png', TRUE, TRUE, NOW(), NOW()),
    ('375d90f3-99be-4471-92f9-cab540ef3c48', 'c0000000-0000-0000-0000-000000000006', '1 Liter Mango', '١ لتر عصير مانجا', NULL, NULL, 180.00, NULL, '/uploads/menu-items/5d49672e-3aaa-4f17-9296-8c3b639784c6.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000017', 'c0000000-0000-0000-0000-000000000006', 'Orange Juice', 'كوب عصير برتقال', NULL, NULL, 70.00, NULL, '/uploads/menu-items/65c9b36f-f30b-465d-a892-df0f91532621.png', TRUE, TRUE, NOW(), NOW()),
    ('7baca0fd-1261-4272-a3d9-92d0f0e70c99', 'c0000000-0000-0000-0000-000000000006', 'Mango Juice', 'كوب عصير مانجا', NULL, NULL, 90.00, NULL, '/uploads/menu-items/2ea48a48-f8d0-4868-9987-74d4b0964fdf.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000001a', 'c0000000-0000-0000-0000-000000000006', 'Ice Cubes Cup', 'كوب مكعبات ثلج', NULL, NULL, 15.00, NULL, '/uploads/menu-items/3b92493b-9f16-4b3d-b596-d0a97e4cb6db.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000001b', 'c0000000-0000-0000-0000-000000000006', 'Water Bottle', 'زجاجة مياه', NULL, NULL, 15.00, NULL, '/uploads/menu-items/8fa23dab-d19f-4d43-a668-7fdf248fda67.png', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    description_en = EXCLUDED.description_en,
    description_ar = EXCLUDED.description_ar,
    current_price = EXCLUDED.current_price,
    mini_price = EXCLUDED.mini_price,
    image_url = EXCLUDED.image_url,
    is_available = EXCLUDED.is_available,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- =========================================================================
-- Category: Offers (عروض) - 6 items
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-00000000001f', 'c0000000-0000-0000-0000-000000000007', 'Friends Meal', 'وجبة الأصدقاء', '2 Pizzas of your choice - 1 Small Pizza Free - 1 Liter (Mix Cola or Juice) Free
<!--RS_OFFER_CONFIG:{"isCustomizable":true,"discountPercentage":0,"discountTarget":"CHEAPEST_ITEM","freeItemTextEn":"Free Drink, Free Pizza","freeItemTextAr":"بيتزا ومشروب مجانًا","buyQuantity":2,"getQuantity":1,"targetGroupId":"grp_1790517430745_rdg2i","choiceGroups":[{"id":"grp_1790517430745_rdg2i","categoryId":"c0000000-0000-0000-0000-000000000001","categoryNameEn":"Pizza","categoryNameAr":"بيتزا","count":3,"selectionMode":"ALL","isFree":false,"pricingMode":"PAID","hasMiniPizza":true,"miniPizzaCount":1,"miniPizzaAllowedItemIds":["d0000000-0000-0000-0000-000000000001","a6c42daf-b0d9-44ea-ab21-660191f27d55","d0000000-0000-0000-0000-000000000004","d0000000-0000-0000-0000-000000000006","d0000000-0000-0000-0000-000000000002","d0000000-0000-0000-0000-000000000003","a30dfd0d-c89b-418d-bc5b-18b8b9fcf656"],"isMiniPizzaFree":true},{"id":"grp_1790530886286_owtmp","categoryId":"c0000000-0000-0000-0000-000000000006","categoryNameEn":"Drinks","categoryNameAr":"مشروبات","count":1,"selectionMode":"SPECIFIC","allowedItemIds":["ffa48f05-fae4-46d2-9865-7c0c81605ec2"],"isFree":true,"pricingMode":"FREE","hasMiniPizza":false}]}-->', '٢ بيتزا من اختيارك - ١ بيتزا صغير هدية - ١ لتر (مكس كولا او عصير) هدية', 0.00, NULL, '/uploads/menu-items/22202978-3d9f-4ed8-9b43-cf8acf71163d.png', TRUE, TRUE, NOW(), NOW()),
    ('96f3da30-562c-43f0-a364-378ee9dc936d', 'c0000000-0000-0000-0000-000000000007', 'Duo Meal', 'وجبة ثنائية', 'Pizza + Pasta + FREE Jalapeño Bites
<!--RS_OFFER_CONFIG:{"isCustomizable":true,"discountPercentage":0,"discountTarget":"TOTAL_BUNDLE","freeItemTextEn":"Free Jalapeno Bites","choiceGroups":[{"id":"grp_1790542964282_q3mky","categoryId":"c0000000-0000-0000-0000-000000000001","categoryNameEn":"Pizza","categoryNameAr":"بيتزا","count":1,"selectionMode":"ALL","isFree":false,"pricingMode":"PAID","hasMiniPizza":false},{"id":"grp_1790542978036_tq7uh","categoryId":"c0000000-0000-0000-0000-000000000002","categoryNameEn":"Pasta","categoryNameAr":"باستا","count":1,"selectionMode":"ALL","isFree":false,"pricingMode":"PAID","hasMiniPizza":false},{"id":"grp_1790542980567_g4d3d","categoryId":"c0000000-0000-0000-0000-000000000004","categoryNameEn":"Appetizers","categoryNameAr":"مقبلات","count":1,"selectionMode":"SPECIFIC","allowedItemIds":["14ba33b8-abfb-4871-803c-10151a22db3c"],"isFree":true,"pricingMode":"FREE","hasMiniPizza":false}]}-->', 'بيتزا + باستا + أصابع هالابينو مجانًا', 0.00, NULL, '/uploads/menu-items/38c4e973-82ab-4f27-8c48-f116d3ce0b74.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000001e', 'c0000000-0000-0000-0000-000000000007', 'Family Meal', 'وجبة العائلة', '3 Pizzas of your choice - 2 Sauces of your choice - 1 Free Pizza - 1 Liter (Mix Cola or Juice) Free.
<!--RS_OFFER_CONFIG:{"isCustomizable":true,"discountPercentage":100,"discountTarget":"CHEAPEST_ITEM","freeItemTextEn":"Free Pizza and Free drink","freeItemTextAr":"بيتزا مجانية ومشروب مجاني","buyQuantity":3,"getQuantity":1,"targetGroupId":"grp_1790517178356_b0dma","choiceGroups":[{"id":"grp_1790517178356_b0dma","categoryId":"c0000000-0000-0000-0000-000000000001","categoryNameEn":"Pizza","categoryNameAr":"بيتزا","count":4,"selectionMode":"ALL","isFree":false,"pricingMode":"CHEAPEST_FREE","freeCount":1,"hasMiniPizza":false},{"id":"grp_1790534315374_9dgac","categoryId":"c0000000-0000-0000-0000-000000000008","categoryNameEn":"Sauce","categoryNameAr":"صوص","count":2,"selectionMode":"ALL","isFree":true,"pricingMode":"FREE","hasMiniPizza":false},{"id":"grp_1790534348702_fsmge","categoryId":"c0000000-0000-0000-0000-000000000006","categoryNameEn":"Drinks","categoryNameAr":"مشروبات","count":1,"selectionMode":"SPECIFIC","allowedItemIds":["ffa48f05-fae4-46d2-9865-7c0c81605ec2"],"isFree":true,"pricingMode":"FREE","hasMiniPizza":false}]}-->', '٣ بيتزا من اختيارك - ٢ صوص من اختيارك - ١ بيتزا هدية - ١ لتر (مكس كولا او عصير) هدية', 0.00, NULL, '/uploads/menu-items/0383f930-cb85-4c3d-bd6d-c55797b09b62.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000001c', 'c0000000-0000-0000-0000-000000000007', 'Kids Meal', 'وجبة الأطفال', 'Small Pizza (sea food excluded) + packet fries + orange juice + toy.
<!--RS_OFFER_CONFIG:{"isCustomizable":true,"discountPercentage":0,"discountTarget":"FIXED_PRICE","fixedPrice":250,"choiceGroups":[{"id":"grp_1790523964391_485cx","categoryId":"c0000000-0000-0000-0000-000000000001","categoryNameEn":"Pizza","categoryNameAr":"بيتزا","count":1,"selectionMode":"SPECIFIC","allowedItemIds":["d0000000-0000-0000-0000-000000000002","d0000000-0000-0000-0000-000000000001","a30dfd0d-c89b-418d-bc5b-18b8b9fcf656","d0000000-0000-0000-0000-000000000003","d0000000-0000-0000-0000-000000000004","d0000000-0000-0000-0000-000000000006"],"isFree":false,"pricingMode":"PAID","hasMiniPizza":false}]}-->', 'بيتزا صغيرة (عدا السي فود) - باكت بطاطس - عصير برتقال - لعبة هدية', 250.00, NULL, '/uploads/menu-items/9bad3f14-13e6-450f-b4bc-2c757d5de495.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000028', 'c0000000-0000-0000-0000-000000000007', 'Single Meal', 'وجبة فردية', '1 Pizza of your choice - 1 Salad of your choice.
<!--RS_OFFER_CONFIG:{"isCustomizable":true,"discountPercentage":0,"discountTarget":"TOTAL_BUNDLE","choiceGroups":[{"id":"grp_1790516647411_b86fl","categoryId":"c0000000-0000-0000-0000-000000000001","categoryNameEn":"Pizza","categoryNameAr":"بيتزا","count":1,"selectionMode":"ALL","isFree":false,"pricingMode":"PAID","hasMiniPizza":false},{"id":"grp_1790516663957_khihz","categoryId":"c0000000-0000-0000-0000-000000000003","categoryNameEn":"Salad","categoryNameAr":"سلطة","count":1,"selectionMode":"ALL","isFree":false,"pricingMode":"PAID","hasMiniPizza":false},{"id":"grp_1790539739215_hresc","categoryId":"c0000000-0000-0000-0000-000000000006","categoryNameEn":"Drinks","categoryNameAr":"مشروبات","titleEn":"Free V Cola","count":1,"selectionMode":"SPECIFIC","allowedItemIds":["d0000000-0000-0000-0000-000000000018"],"isFree":true,"pricingMode":"FREE","hasMiniPizza":false},{"id":"grp_1790539810290_puwo1","categoryId":"c0000000-0000-0000-0000-000000000004","categoryNameEn":"Appetizers","categoryNameAr":"مقبلات","count":1,"selectionMode":"SPECIFIC","allowedItemIds":["d0000000-0000-0000-0000-000000000014"],"isFree":true,"pricingMode":"FREE","hasMiniPizza":false}]}-->', '١ بيتزا من اختيارك - ١ سلطة من اختيارك', 0.00, NULL, '/uploads/menu-items/c746e42a-6ae5-45f2-b9e2-4b729f9107e3.png', TRUE, TRUE, NOW(), NOW()),
    ('2e2a4dd2-248d-4fbe-a780-46e46fee6d5a', 'c0000000-0000-0000-0000-000000000007', 'Pasta 2 Go', 'باستا 2 جو', '2 Pasta,  Caesar Salad(Free) and French fries (Free)
<!--RS_OFFER_CONFIG:{"isCustomizable":true,"discountPercentage":0,"discountTarget":"TOTAL_BUNDLE","freeItemTextEn":"Free Caesar Salad and Free French Fries","freeItemTextAr":"سلطة سيزر وبطاطس مقلية مجانًا","choiceGroups":[{"id":"grp_1790538040669_4x9o8","categoryId":"c0000000-0000-0000-0000-000000000002","categoryNameEn":"Pasta","categoryNameAr":"باستا","count":2,"selectionMode":"ALL","isFree":false,"pricingMode":"PAID","hasMiniPizza":false},{"id":"grp_1790538057289_h81sl","categoryId":"c0000000-0000-0000-0000-000000000003","categoryNameEn":"Salad","categoryNameAr":"سلطة","count":1,"selectionMode":"SPECIFIC","allowedItemIds":["d0000000-0000-0000-0000-00000000000e"],"isFree":true,"pricingMode":"FREE","hasMiniPizza":false},{"id":"grp_1790538081517_qf7rt","categoryId":"c0000000-0000-0000-0000-000000000004","categoryNameEn":"Appetizers","categoryNameAr":"مقبلات","count":1,"selectionMode":"SPECIFIC","allowedItemIds":["d0000000-0000-0000-0000-000000000014"],"isFree":true,"pricingMode":"FREE","hasMiniPizza":false}]}-->', 'طبقين من الباستا، سلطة سيزر (مجانًا)، وبطاطس مقلية (مجانًا)', 0.00, NULL, '/uploads/menu-items/eba5f09b-5728-4775-a402-36a06fe7a4b2.png', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    description_en = EXCLUDED.description_en,
    description_ar = EXCLUDED.description_ar,
    current_price = EXCLUDED.current_price,
    mini_price = EXCLUDED.mini_price,
    image_url = EXCLUDED.image_url,
    is_available = EXCLUDED.is_available,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- =========================================================================
-- Category: Sauce (صوص) - 5 items
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000024', 'c0000000-0000-0000-0000-000000000008', 'Ranch', 'رانش', NULL, NULL, 20.00, NULL, '/uploads/menu-items/b18e9a8b-1871-4cf5-8fab-297fc425f927.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000025', 'c0000000-0000-0000-0000-000000000008', 'Mayonnaise', 'مايونيز', NULL, NULL, 20.00, NULL, '/uploads/menu-items/fb24bc79-85c5-4ff5-9c75-80fa3e7b513b.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000027', 'c0000000-0000-0000-0000-000000000008', 'Caesar Dressing', 'صلصة السيزر', NULL, NULL, 30.00, NULL, '/uploads/menu-items/1d7d037f-24a5-4b02-aba5-fa9e3be3db00.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000023', 'c0000000-0000-0000-0000-000000000008', 'Barbecue', 'باربيكيو', NULL, NULL, 20.00, NULL, '/uploads/menu-items/dea77ae1-f17a-42dc-a241-fe0ed0a122ca.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000026', 'c0000000-0000-0000-0000-000000000008', 'Mustard', 'مستردة', NULL, NULL, 20.00, NULL, '/uploads/menu-items/0fc8cbec-6e61-463e-a21c-aff31072a26f.png', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    description_en = EXCLUDED.description_en,
    description_ar = EXCLUDED.description_ar,
    current_price = EXCLUDED.current_price,
    mini_price = EXCLUDED.mini_price,
    image_url = EXCLUDED.image_url,
    is_available = EXCLUDED.is_available,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- =========================================================================
-- Add-ons (10 add-ons)
-- =========================================================================
DELETE FROM menu_addons WHERE id NOT IN ('b5b4d2a6-7e03-4ca9-9ffc-feb38efc342d', 'ccb317aa-0d0a-48e2-8599-7c4b0d24ef55', 'a4145816-6e57-42ef-b2f6-59faa9dd3115', '47ac5a38-c771-4cc2-9553-0f1355e9a0fd', '913904d2-02ee-4658-923b-9d3132891508', '1d0e1e26-4de3-42f3-8836-b5dc6071cdbd', '2402c437-d5b8-4f92-b4dd-8dc29883aa06', '84bdd0b6-a44e-471f-b263-7da6392015ed', '9eba126c-183d-40ca-986c-0f86edb0ad94', '902fb306-d69e-44ca-9fa4-d6cc3ee82f58');

INSERT INTO menu_addons (id, menu_item_id, name_en, name_ar, price, is_active, created_at, updated_at)
VALUES
    ('b5b4d2a6-7e03-4ca9-9ffc-feb38efc342d', 'd0000000-0000-0000-0000-000000000002', 'Burrata', 'بوراتا', 150.00, TRUE, NOW(), NOW()),
    ('ccb317aa-0d0a-48e2-8599-7c4b0d24ef55', 'e5a40487-a76d-4232-8964-a05ee364c807', 'Extra Sauce ( Strawberry )', 'اضافة صوص ( فراوله )', 20.00, TRUE, NOW(), NOW()),
    ('a4145816-6e57-42ef-b2f6-59faa9dd3115', 'e5a40487-a76d-4232-8964-a05ee364c807', 'Extra Sauce ( Nutella )', 'اضافة صوص ( نيوتيلا )', 20.00, TRUE, NOW(), NOW()),
    ('47ac5a38-c771-4cc2-9553-0f1355e9a0fd', 'd0000000-0000-0000-0000-000000000008', 'Shrimp', 'جمبري', 80.00, TRUE, NOW(), NOW()),
    ('913904d2-02ee-4658-923b-9d3132891508', 'd0000000-0000-0000-0000-000000000008', 'Chicken', 'دجاج', 40.00, TRUE, NOW(), NOW()),
    ('1d0e1e26-4de3-42f3-8836-b5dc6071cdbd', 'd0000000-0000-0000-0000-000000000008', 'Salmon', 'سالمون', 120.00, TRUE, NOW(), NOW()),
    ('2402c437-d5b8-4f92-b4dd-8dc29883aa06', 'd0000000-0000-0000-0000-00000000000e', 'Shrimp', 'جمبري', 100.00, TRUE, NOW(), NOW()),
    ('84bdd0b6-a44e-471f-b263-7da6392015ed', 'd0000000-0000-0000-0000-00000000000e', 'Salmon', 'سالمون', 150.00, TRUE, NOW(), NOW()),
    ('9eba126c-183d-40ca-986c-0f86edb0ad94', 'd0000000-0000-0000-0000-00000000000e', 'Chicken', 'دجاج', 50.00, TRUE, NOW(), NOW()),
    ('902fb306-d69e-44ca-9fa4-d6cc3ee82f58', 'd0000000-0000-0000-0000-000000000010', 'Burrata', 'بوراتا', 100.00, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    menu_item_id = EXCLUDED.menu_item_id,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    price = EXCLUDED.price,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- =========================================================================
-- Synchronize original_price and discount_price (if columns exist)
-- =========================================================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'menu_items' AND column_name = 'discount_price'
    ) THEN
        UPDATE menu_items SET original_price = 300.00, discount_price = 240.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000002';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'e5a40487-a76d-4232-8964-a05ee364c807';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000024';
        UPDATE menu_items SET original_price = 350.00, discount_price = 250.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000000a';
        UPDATE menu_items SET original_price = 350.00, discount_price = 280.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000008';
        UPDATE menu_items SET original_price = 350.00, discount_price = 200.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000016';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'ffa48f05-fae4-46d2-9865-7c0c81605ec2';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000015';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000025';
        UPDATE menu_items SET original_price = 500.00, discount_price = 400.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000000d';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'ae7e23d3-7dad-425a-a8cf-bb6c78504e8f';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000012';
        UPDATE menu_items SET original_price = 400.00, discount_price = 320.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000003';
        UPDATE menu_items SET original_price = 500.00, discount_price = 400.00, updated_at = NOW() WHERE id = 'a30dfd0d-c89b-418d-bc5b-18b8b9fcf656';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000001f';
        UPDATE menu_items SET original_price = 200.00, discount_price = 180.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000011';
        UPDATE menu_items SET original_price = 300.00, discount_price = 230.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000000f';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000019';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = '96f3da30-562c-43f0-a364-378ee9dc936d';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000001e';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000018';
        UPDATE menu_items SET original_price = 450.00, discount_price = 360.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000004';
        UPDATE menu_items SET original_price = 450.00, discount_price = 360.00, updated_at = NOW() WHERE id = 'a6c42daf-b0d9-44ea-ab21-660191f27d55';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000027';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = '375d90f3-99be-4471-92f9-cab540ef3c48';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000000e';
        UPDATE menu_items SET original_price = 450.00, discount_price = 360.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000000c';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000017';
        UPDATE menu_items SET original_price = 70.00, discount_price = 50.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000014';
        UPDATE menu_items SET original_price = 480.00, discount_price = 380.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000001';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000001c';
        UPDATE menu_items SET original_price = 150.00, discount_price = 120.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000013';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000028';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = '2e2a4dd2-248d-4fbe-a780-46e46fee6d5a';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = '7baca0fd-1261-4272-a3d9-92d0f0e70c99';
        UPDATE menu_items SET original_price = 650.00, discount_price = 500.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000009';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000023';
        UPDATE menu_items SET original_price = 650.00, discount_price = 520.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000005';
        UPDATE menu_items SET original_price = 350.00, discount_price = 280.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000000b';
        UPDATE menu_items SET original_price = 280.00, discount_price = 220.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000010';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = '14ba33b8-abfb-4871-803c-10151a22db3c';
        UPDATE menu_items SET original_price = 450.00, discount_price = 360.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000006';
        UPDATE menu_items SET original_price = 320.00, discount_price = 230.00, updated_at = NOW() WHERE id = 'c317ff40-a2a3-42fb-a8a3-d88b42f676c0';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000026';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000001a';
        UPDATE menu_items SET original_price = 600.00, discount_price = 480.00, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-000000000007';
        UPDATE menu_items SET original_price = NULL, discount_price = NULL, updated_at = NOW() WHERE id = 'd0000000-0000-0000-0000-00000000001b';
    END IF;
END $$;
