-- liquibase formatted sql

-- changeset resapori:28-seed-menu-data runOnChange:true
-- comment: Seed initial menu categories, items, and add-ons idempotently matching frontend dev branch
-- validCheckSum: ANY
-- validCheckSum: 9:712a794ce61ba37a75a54c24d0532d0f

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
-- Category: Pizza (بيتزا)
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'Chicken Pesto', 'دجاج بيستو', 'Pesto + white sauce + chicken + pesto sauce + mozzarella + parmesan.', 'صلصة بيستو + صلصة بيضاء + دجاج + بيستو + موتزاريلا + بارميزان.', 480.00, NULL, '/assets/menu_images/pestonobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'Margherita', 'مارجريتا', 'Tomato sauce + basil + mozzarella + parmesan.', 'صلصة طماطم + ريحان + موتزاريلا + بارميزان.', 300.00, NULL, '/assets/menu_images/margheritanobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001', 'Vegetarian', 'خضروات', 'Tomato sauce + cherry tomatoes + sweet corn + basil + mozzarella + parmesan.', 'صلصة طماطم + طماطم شيري + ذرة حلوة + ريحان + موتزاريلا + بارميزان.', 400.00, NULL, '/assets/menu_images/vegetariannobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000001', 'Pepperoni', 'بيبروني', 'Tomato sauce + jalapeño + salami + basil + mozzarella + parmesan.', 'صلصة طماطم + هلابينو + سلامي + ريحان + موتزاريلا + بارميزان.', 450.00, NULL, '/assets/menu_images/pepperoninobackground2.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000001', 'Creamy Salmon', 'كريمي سالمون', 'White sauce + salmon + shrimp + Kiri + basil + mozzarella + parmesan.', 'صلصة بيضاء + سالمون + جمبري + كيري + ريحان + موتزاريلا + بارميزان.', 650.00, NULL, '/assets/menu_images/salmonpizzanobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000001', 'Quattro', 'كواترو', 'White sauce + blue cheese + Gouda cheese + red cheddar + honey + basil + mozzarella + parmesan.', 'صلصة بيضاء + جبنة ريكفورد + جبنة جودة + تشيدر أحمر + عسل + ريحان + موتزاريلا + بارميزان.', 450.00, NULL, '/assets/menu_images/quatronobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000001', 'Frutti di Mare', 'فروتي دي ماري', 'White sauce + shrimp + calamari + sea mussels + mozzarella + parmesan.', 'صلصة بيضاء + جمبري + كالماري + بلح البحر + موتزاريلا + بارميزان.', 600.00, NULL, '/assets/menu_images/frutydemarynobackground.png', TRUE, TRUE, NOW(), NOW())
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
-- Category: Pasta (باستا)
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000002', 'Pesto', 'بيستو', 'Penne + white sauce + pesto sauce + pesto + parmesan.', 'مكرونة + صلصة بيضاء + صلصة بيستو + بيستو + بارميزان.', 350.00, NULL, '/assets/menu_images/pestopastanobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000009', 'c0000000-0000-0000-0000-000000000002', 'Creamy Salmon', 'كريمي سالمون', 'Penne + tomato sauce + fresh cream + capers + salmon + pesto + parmesan.', 'مكرونة + صلصة طماطم + كريمة طازجة + كابري + سالمون + بيستو + بارميزان.', 650.00, NULL, '/assets/menu_images/salmonnobackgroundpasta.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000000a', 'c0000000-0000-0000-0000-000000000002', 'Alfredo', 'ألفريدو', 'Fettuccine + white sauce + chicken + mushroom + parmesan.', 'فيتوتشيني + صوص أبيض + دجاج + مشروم + جبنة بارميزان.', 370.00, NULL, '/assets/menu_images/alfredonobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000000b', 'c0000000-0000-0000-0000-000000000002', 'Bolognese', 'بولونيز', 'Spaghetti + tomato sauce + bolognese + cherry tomatoes + pesto + parmesan.', 'سباجيتي + صلصة طماطم + بولونيز + طماطم شيري + بيستو + بارميزان.', 350.00, NULL, '/assets/menu_images/polonaisenobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000000c', 'c0000000-0000-0000-0000-000000000002', 'Signature', 'توقيعنا (سيجنتشر)', 'Penne + pepperoni + chicken + red cheddar + Gouda cheese + white sauce + pesto + parmesan.', 'بيني + بيبروني + دجاج + تشيدر أحمر + جبنة جودة + صلصة بيضاء + بيستو + بارميزان.', 500.00, NULL, '/assets/menu_images/signaturenobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000000d', 'c0000000-0000-0000-0000-000000000002', 'Re Sapori', 'ري سابوري', 'Penne + shrimp + chicken + mushroom + white sauce + onion + garlic + pesto + parmesan.', 'بيني + جمبري + دجاج + مشروم + صلصة بيضاء + بصل + ثوم + بيستو + بارميزان.', 550.00, NULL, '/assets/menu_images/resaporipastanobackground.png', TRUE, TRUE, NOW(), NOW())
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
-- Category: Salad (سلطة)
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-00000000000e', 'c0000000-0000-0000-0000-000000000003', 'Caesar', 'سيزر', 'Kabocha + Caesar sauce + toast + parmesan.', 'كابوتشا + صلصة سيزر + توست + بارميزان.', 150.00, NULL, '/assets/menu_images/caesarsaladnobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000000f', 'c0000000-0000-0000-0000-000000000003', 'Kenwa', 'كينوا', 'Batavia + quinoa + sweet corn + goat cheese + honey mustard.', 'باتافيا + كينوا + ذرة حلوة + جبنة ماعز + خردل بالعسل.', 300.00, NULL, '/assets/menu_images/kenwasaladnobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000010', 'c0000000-0000-0000-0000-000000000003', 'Apple Sapori', 'تفاح سابوري', 'Batavia + honey mustard + green apple + goat cheese + dried figs + watercress.', 'باتافيا + خردل بالعسل + تفاح أخضر + جبنة ماعز + تين مجفف + جرجير.', 280.00, NULL, '/assets/menu_images/applesaporinobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000003', 'Rocca', 'روكا', 'Cherry tomatoes + mushroom + lemon dressing + parmesan + watercress.', 'طماطم شيري + مشروم + صوص الليمون + بارميزان + جرجير.', 200.00, NULL, '/assets/menu_images/roccasaladnobackground.png', TRUE, TRUE, NOW(), NOW())
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
-- Category: Appetizers (مقبلات)
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000012', 'c0000000-0000-0000-0000-000000000004', 'Onion Rings', 'حلقات البصل', '6 pieces', '6 قطع', 100.00, NULL, '/assets/menu_images/onionrings.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000013', 'c0000000-0000-0000-0000-000000000004', 'Mozzarella Sticks', 'أصابع الموتزاريلا', '6 pieces', '6 قطع', 150.00, NULL, '/assets/menu_images/mozzarellasticks.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000014', 'c0000000-0000-0000-0000-000000000004', 'French Fries', 'بطاطس مقلية', '', '', 70.00, NULL, '/assets/menu_images/frenchfries.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000015', 'c0000000-0000-0000-0000-000000000004', 'Curly Fries', 'بطاطس كيرلي', '', '', 120.00, NULL, '/assets/menu_images/curlyfries.png', TRUE, TRUE, NOW(), NOW())
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
-- Category: Desserts (حلويات)
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000016', 'c0000000-0000-0000-0000-000000000005', 'Calzone', 'كالزوني', 'Italian pizza dough + banana + powdered sugar + Nutella + chocolate + cocoa.', 'عجينة بيتزا إيطالية + موز + سكر بودرة + نوتيلا + شوكولاتة + كاكاو.', 350.00, 200.00, '/assets/menu_images/calazonenobackground.png', TRUE, TRUE, NOW(), NOW())
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
-- Category: Drinks (مشروبات)
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000017', 'c0000000-0000-0000-0000-000000000006', 'Fresh Juice', 'عصير فريش', '', '', 90.00, NULL, '/assets/menu_images/orangejuice.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000018', 'c0000000-0000-0000-0000-000000000006', 'Can Drink', 'مشروب غازي', '', '', 25.00, NULL, '/assets/menu_images/vcola.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000019', 'c0000000-0000-0000-0000-000000000006', 'Red Bull', 'ريد بول', '', '', 85.00, NULL, '/assets/menu_images/redbull.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000001a', 'c0000000-0000-0000-0000-000000000006', 'Ice Cubes Cup', 'كوب مكعبات ثلج', '', '', 15.00, NULL, '/assets/menu_images/icecubes.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000001b', 'c0000000-0000-0000-0000-000000000006', 'Water Bottle', 'زجاجة مياه', '', '', 15.00, NULL, '/assets/menu_images/waterbottle.png', TRUE, TRUE, NOW(), NOW())
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
-- Category: Offers (عروض)
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-00000000001c', 'c0000000-0000-0000-0000-000000000007', 'Kids Meal', 'وجبة أطفال', 'Mini (Margherita only) + orange juice + toy + 1 sauce.', 'ميني (مارجريتا فقط) + عصير برتقال + لعبة + صوص واحد.', 250.00, NULL, '/assets/menu_images/kidsmeal.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000001d', 'c0000000-0000-0000-0000-000000000007', 'Box', 'صندوق (بوكس)', '2 Mini Pizzas (1 Margherita) + your choice of 1 Pizza: Chicken Pesto, Pepperoni, or Quattro + 2 sauces of your choice + packet fries + V7 can.', '٢ بيتزا ميني (منهم ١ مارجريتا) + اختيارك لبيتزا واحدة: تشيكن بيستو، بيبروني، أو كواترو + ٢ صوص من اختيارك + بطاطس باكيت + مشروب غازي.', 480.00, NULL, '/assets/menu_images/boxoffer.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000001e', 'c0000000-0000-0000-0000-000000000007', 'Family Meal', 'وجبة عائلية', 'Chicken Pesto + Quattro + Vegetarian + 2 Fries + 3 Sauces + 1 Liter Sina Cola.', 'تشيكن بيستو + كواترو + خضروات + 2 بطاطس مقلية + 3 صوصات + سينا كولا ١ لتر.', 1300.00, NULL, '/assets/menu_images/familymeal.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-00000000001f', 'c0000000-0000-0000-0000-000000000007', 'Friends Meal', 'وجبة الأصدقاء', 'Vegetarian + 1 Liter Sina Cola + Onion Rings + Curly Fries.', 'خضروات + سينا كولا ١ لتر + حلقات البصل + بطاطس كيرلي.', 600.00, NULL, '/assets/menu_images/friendsmeal.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000020', 'c0000000-0000-0000-0000-000000000007', 'Double Deal', 'دبل ديل', 'Margherita Pizza + Bolognese Pasta + 1 Chicken Caesar Salad Free + 2 Drinks + 2 Sauces.', 'بيتزا مارجريتا + باستا بولونيز + سلطة سيزر بالدجاج مجاناً + 2 مشروب غازي + 2 صوص.', 900.00, NULL, '/assets/menu_images/doubledeal.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000021', 'c0000000-0000-0000-0000-000000000007', 'Group Meal', 'وجبة جماعية', 'Margherita + Pepperoni + 1 Liter Sina Cola + Onion Rings + Mozzarella Sticks.', 'مارجريتا + بيبروني + سيناكولا ١ لتر + حلقات البصل + أصابع الموتزاريلا.', 900.00, NULL, '/assets/menu_images/groupmeal.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000022', 'c0000000-0000-0000-0000-000000000007', 'Double Trip', 'دبل تريب', 'Alfredo Pasta + Signature Pasta + Rocca Salad Free.', 'باستا ألفريدو + باستا سيجنتشر + سلطة روكا مجاناً.', 900.00, NULL, '/assets/menu_images/doubletrip.png', TRUE, TRUE, NOW(), NOW())
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
-- Category: Sauce (صوص)
-- =========================================================================
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000023', 'c0000000-0000-0000-0000-000000000008', 'Barbecue', 'باربيكيو', '', '', 20.00, NULL, '/assets/menu_images/barbecue.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000024', 'c0000000-0000-0000-0000-000000000008', 'Ranch', 'رانش', '', '', 20.00, NULL, '/assets/menu_images/ranch.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000025', 'c0000000-0000-0000-0000-000000000008', 'Mayonnaise', 'مايونيز', '', '', 20.00, NULL, '/assets/menu_images/mayonnaise.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000026', 'c0000000-0000-0000-0000-000000000008', 'Mustard', 'مستردة', '', '', 20.00, NULL, '/assets/menu_images/mustard.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000027', 'c0000000-0000-0000-0000-000000000008', 'Caesar Dressing', 'صلصة السيزر', '', '', 30.00, NULL, '/assets/menu_images/caesardressing.png', TRUE, TRUE, NOW(), NOW())
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
-- Add-ons (8 add-ons)
-- =========================================================================
INSERT INTO menu_addons (id, menu_item_id, name_en, name_ar, price, is_active, created_at, updated_at)
VALUES
    ('e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000002', 'Burrata', 'بوراتا', 150.00, TRUE, NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000008', 'Chicken', 'دجاج', 50.00, TRUE, NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000008', 'Shrimp', 'جمبري', 100.00, TRUE, NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000008', 'Salmon', 'سالمون', 150.00, TRUE, NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-00000000000e', 'Chicken', 'دجاج', 50.00, TRUE, NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-00000000000e', 'Shrimp', 'جمبري', 100.00, TRUE, NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-00000000000e', 'Salmon', 'سالمون', 150.00, TRUE, NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000008', 'd0000000-0000-0000-0000-000000000010', 'Burrata', 'بوراتا', 100.00, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    menu_item_id = EXCLUDED.menu_item_id,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    price = EXCLUDED.price,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();
