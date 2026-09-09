-- liquibase formatted sql

-- changeset resapori:28-seed-menu-data
-- comment: Seed initial menu categories, items, and add-ons idempotently

-- 1. Categories
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
ON CONFLICT (id) DO NOTHING;

-- 2. Pizza Items
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'Chicken Pesto', 'دجاج بيستو', 'Pesto + white sauce + chicken + pesto sauce + mozzarella + parmesan.', 'صلصة بيستو + صلصة بيضاء + دجاج + بيستو + موتزاريلا + بارميزان.', 450.00, NULL, '/assets/menu_images/pestonobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'Margherita', 'مارجريتا', 'Tomato sauce + basil + mozzarella + parmesan.', 'صلصة طماطم + ريحان + موتزاريلا + بارميزان.', 300.00, NULL, '/assets/menu_images/margheritanobackground.avif', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001', 'Vegetarian', 'خضروات', 'Tomato sauce + cherry tomatoes + sweet corn + basil + mozzarella + parmesan.', 'صلصة طماطم + طماطم شيري + ذرة حلوة + ريحان + موتزاريلا + بارميزان.', 400.00, NULL, '/assets/menu_images/mushroomnobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000001', 'Pepperoni', 'ببروني', 'Tomato sauce + jalapeño + salami + basil + mozzarella + parmesan.', 'صلصة طماطم + هلابينو + سلامي + ريحان + موتزاريلا + بارميزان.', 500.00, NULL, '/assets/menu_images/pepperoninobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000001', 'Creamy Salmon', 'كريمي سالمون', 'White sauce + salmon + shrimp + Kiri + basil + mozzarella + parmesan.', 'صلصة بيضاء + سالمون + جمبري + كيري + ريحان + موتزاريلا + بارميزان.', 650.00, NULL, '/assets/menu_images/salmonnobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000001', 'Quattro', 'كواترو', 'White sauce + blue cheese + Gouda cheese + red cheddar + honey + basil + mozzarella + parmesan.', 'صلصة بيضاء + جبنة ريكفورد + جبنة جودة + تشيدر أحمر + عسل + ريحان + موتزاريلا + بارميزان.', 450.00, NULL, '/assets/menu_images/quatronobackground.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000001', 'Frutti di Mare', 'فروتي دي ماري', 'White sauce + shrimps + kalemari + bala7 el ba7r + mozzarella + parmesan.', 'صلصة بيضاء + جمبري + كالماري + بلح البحر + موتزاريلا + بارميزان.', 600.00, NULL, '/assets/menu_images/frutydemarynobackground.png', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 3. Pasta Items
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000002', 'Pesto', 'بيستو', 'Penne + white sauce + pesto sauce + pesto + parmesan.', 'مكرونه + صلصة بيضاء + صلصة بيستو + بيستو + بارميزان.', 350.00, NULL, '/assets/menu_images/pesto pasta.jpeg', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000009', 'c0000000-0000-0000-0000-000000000002', 'Creamy Salmon', 'كريمي سالمون', 'Penne + tomato sauce + fresh cream + capers + salmon + pesto + parmesan.', 'مكرونه + صلصة طماطم + كريمة طازجة + كابري + سلامون + بيستو + بارميزان.', 800.00, NULL, '/assets/menu_images/seafoodpasta.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000010', 'c0000000-0000-0000-0000-000000000002', 'Alfredo', 'ألفريدو', 'Fettuccine + White Sauce + Chicken + Mushrooms + Parmesan.', 'فيتوتشيني + صوص أبيض + دجاج + مشروم + جبنة بارميزان.', 370.00, NULL, '/assets/menu_images/seafoodpasta.png', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000002', 'Polonaise', 'بولونيز', 'Spaghetti + tomato sauce + Polonaise + cherry tomato + pesto + parmesan.', 'اسباجيتي + صلصة طماطم + بولونيز + طماطم شيري + بيستو + بارميزان.', 350.00, NULL, '/assets/menu_images/Polomleze.jpeg', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000012', 'c0000000-0000-0000-0000-000000000002', 'Signature', 'توقيعنا (سيجنتشر)', 'Penne + pepperoni + chicken + red cheddar + Gouda cheese + white sauce + pesto + parmesan.', 'بينا + ببروني + دجاج + تشيدر أحمر + جبنة جودة + صلصة بيضاء + بيستو + بارميزان.', 500.00, NULL, '/assets/menu_images/pasta 3.jpeg', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000013', 'c0000000-0000-0000-0000-000000000002', 'Re Sapori', 'ري سابوري', 'Penne + shrimp + chicken + mushroom + white sauce + onions + garlic + pesto + parmesan.', 'بينا + جمبري + دجاج + مشروم + صلصة بيضاء + بصل + ثوم + بيستو + بارميزان.', 550.00, NULL, '/assets/menu_images/pasta 2.jpeg', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 4. Salad Items
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000014', 'c0000000-0000-0000-0000-000000000003', 'Caesar', 'سيزر', 'Kabocha + Caesar sauce + toast + parmesan.', 'كابوتشا + صلصة سيزر + توست + بارميزان.', 150.00, NULL, '/assets/menu_images/سيزر سالاد.jpeg', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000015', 'c0000000-0000-0000-0000-000000000003', 'Kenwa', 'كينوا', 'Batavia + quinoa + sweet corn + goat cheese + honey mustard.', 'باتافيا + كينوا + ذرة حلوة + جبن ماعز + خردل بالعسل.', 300.00, NULL, '/assets/menu_images/كينوا سالاد.jpeg', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000016', 'c0000000-0000-0000-0000-000000000003', 'Apple Sapori', 'تفاح سابوري', 'Batavia + honey mustard + green apple + goat cheese + dried figs + watercress.', 'باتافيا + خردل بالعسل + تفاح أخضر + جبن ماعز + تين مجفف + جرجير.', 280.00, NULL, '/assets/menu_images/Re sapori salad.jpeg', TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000017', 'c0000000-0000-0000-0000-000000000003', 'Rocca', 'روكا', 'Cherry tomato + mushroom + lemon dressing + parmesan + watercress.', 'طماطم شيري + مشروم + صوص الليمون + بارميزان + جرجير.', 200.00, NULL, '/assets/menu_images/روكا سالاد.jpeg', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 5. Appetizer Items
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000018', 'c0000000-0000-0000-0000-000000000004', 'Onion Rings', 'حلقات البصل', '6 pieces', '6 قطع', 100.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000019', 'c0000000-0000-0000-0000-000000000004', 'Mozzarella Sticks', 'أصابع الموتزاريلا', '6 pieces', '6 قطع', 150.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000020', 'c0000000-0000-0000-0000-000000000004', 'French Fries', 'بطاطس مقلية', '', '', 70.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000021', 'c0000000-0000-0000-0000-000000000004', 'Curly Fries', 'بطاطس كيرلي', '', '', 120.00, NULL, NULL, TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 6. Dessert Items
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000022', 'c0000000-0000-0000-0000-000000000005', 'Calzone', 'كالزوني', 'Italian Pizza Dough + Banana + Powdered Sugar + Nutella + Chocolate + Cocoa.', 'عجينة بيتزا إيطالية + موز + سكر بودرة + نوتيلا + شوكولاتة + كاكاو', 350.00, 200.00, '/assets/menu_images/calazone.jpeg', TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 7. Drink Items
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000023', 'c0000000-0000-0000-0000-000000000006', 'Fresh Orange', 'برتقال فريش', '', '', 60.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000024', 'c0000000-0000-0000-0000-000000000006', 'Fresh Mango', 'مانجو فريش', '', '', 65.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000025', 'c0000000-0000-0000-0000-000000000006', 'Blueberry Milkshake', 'ميلك شيك التوت الأزرق', '', '', 85.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000026', 'c0000000-0000-0000-0000-000000000006', 'Chocolate Milkshake', 'ميلك شيك الشوكولاتة', '', '', 85.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000027', 'c0000000-0000-0000-0000-000000000006', 'Pepsi', 'بيبسي', '', '', 25.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000028', 'c0000000-0000-0000-0000-000000000006', 'Mountain Dew', 'ماونتن ديو', '', '', 25.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000029', 'c0000000-0000-0000-0000-000000000006', '7UP', 'سفن أب', '', '', 25.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000030', 'c0000000-0000-0000-0000-000000000006', 'Iced Tea', 'شاي مثلج', '', '', 45.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000031', 'c0000000-0000-0000-0000-000000000006', 'Red Bull', 'ريد بل', '', '', 70.00, NULL, NULL, TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 8. Offer Items
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000032', 'c0000000-0000-0000-0000-000000000007', 'Kids Meal', 'وجبة أطفال', 'Mini(margherita only) + orange + toy + 1 sauce', 'ميني (مارجريتا فقط) + برتقال + لعبة + 1 صلصة', 250.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000033', 'c0000000-0000-0000-0000-000000000007', 'Box', 'صندوق (بوكس)', '2 Mini Pizzas (1 Margherita) + Your Choice of 1 Pizza: Chicken Pesto, Pepperoni, or Quattro + 2 Sauces of Your Choice.', '٢ بيتزا ميني (واحدة مارغريتا) + اختيارك لبيتزا واحدة من: تشيكن بيستو، بيبروني أو كواترو + ٢ صوص من اختيارك.', 480.00, NULL, NULL, TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 9. Sauce Items
INSERT INTO menu_items (id, category_id, name_en, name_ar, description_en, description_ar, current_price, mini_price, image_url, is_available, is_active, created_at, updated_at)
VALUES
    ('d0000000-0000-0000-0000-000000000034', 'c0000000-0000-0000-0000-000000000008', 'Barbecue', 'باربيكيو', '', '', 20.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000035', 'c0000000-0000-0000-0000-000000000008', 'Ranch', 'رانش', '', '', 20.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000036', 'c0000000-0000-0000-0000-000000000008', 'Mayonnaise', 'مايونيز', '', '', 20.00, NULL, NULL, TRUE, TRUE, NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000037', 'c0000000-0000-0000-0000-000000000008', 'Mustard', 'مستردة', '', '', 20.00, NULL, NULL, TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 10. Add-ons
-- Margherita Burrata
INSERT INTO menu_addons (id, menu_item_id, name_en, name_ar, price, is_active, created_at, updated_at)
VALUES ('e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000002', 'Burrata', 'بوراتا', 150.00, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Pesto Pasta Add-ons
INSERT INTO menu_addons (id, menu_item_id, name_en, name_ar, price, is_active, created_at, updated_at)
VALUES
    ('e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000008', 'Chicken', 'دجاج', 50.00, TRUE, NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000008', 'Shrimp', 'جمبري', 100.00, TRUE, NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000008', 'Salmon', 'سلمون', 150.00, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Caesar Salad Add-ons
INSERT INTO menu_addons (id, menu_item_id, name_en, name_ar, price, is_active, created_at, updated_at)
VALUES
    ('e0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000014', 'Chicken', 'دجاج', 50.00, TRUE, NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000014', 'Shrimp', 'جمبري', 100.00, TRUE, NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000014', 'Salmon', 'سلمون', 150.00, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Apple Sapori Add-on
INSERT INTO menu_addons (id, menu_item_id, name_en, name_ar, price, is_active, created_at, updated_at)
VALUES ('e0000000-0000-0000-0000-000000000008', 'd0000000-0000-0000-0000-000000000016', 'Burrata', 'بوراتا', 100.00, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
