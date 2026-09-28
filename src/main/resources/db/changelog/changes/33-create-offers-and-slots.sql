-- liquibase formatted sql

-- changeset resapori:33-create-offers-and-slots
-- comment: Create offers, offer_slots, offer_slot_eligible_items tables and alter order_items

CREATE TYPE discount_target AS ENUM ('TOTAL_BUNDLE', 'CHEAPEST_ITEM', 'FIXED_PRICE');

CREATE TABLE offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name_en VARCHAR(255) NOT NULL,
    name_ar VARCHAR(255) NOT NULL,
    description_en TEXT,
    description_ar TEXT,
    image_url VARCHAR(500),
    discount_target discount_target NOT NULL DEFAULT 'CHEAPEST_ITEM',
    discount_percentage DECIMAL(5, 2),
    fixed_price DECIMAL(10, 2),
    buy_quantity INT NOT NULL DEFAULT 1,
    get_quantity INT NOT NULL DEFAULT 0,
    category_id UUID REFERENCES menu_categories(id) ON DELETE SET NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255)
);

CREATE TABLE offer_slots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offer_id UUID NOT NULL REFERENCES offers(id) ON DELETE CASCADE,
    slot_name_en VARCHAR(100) NOT NULL,
    slot_name_ar VARCHAR(100) NOT NULL,
    menu_item_id UUID REFERENCES menu_items(id) ON DELETE RESTRICT,
    category_id UUID REFERENCES menu_categories(id) ON DELETE RESTRICT,
    quantity INT NOT NULL DEFAULT 1,
    display_order INT NOT NULL DEFAULT 0,
    is_free BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255)
);

CREATE TABLE offer_slot_eligible_items (
    slot_id UUID NOT NULL REFERENCES offer_slots(id) ON DELETE CASCADE,
    menu_item_id UUID NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    PRIMARY KEY (slot_id, menu_item_id)
);

ALTER TABLE order_items
    ADD COLUMN offer_id UUID REFERENCES offers(id) ON DELETE SET NULL,
    ADD COLUMN bundle_group_id UUID,
    ADD COLUMN is_free BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_offers_active ON offers(is_active);
CREATE INDEX idx_offer_slots_offer_id ON offer_slots(offer_id);
CREATE INDEX idx_order_items_bundle_group_id ON order_items(bundle_group_id);
