-- liquibase formatted sql

-- changeset resapori:41-alter-orders-add-notes-and-addons
-- comment: Add order_notes to orders, notes to order_items, and create order_item_addons table

ALTER TABLE orders
    ADD COLUMN order_notes TEXT;

ALTER TABLE order_items
    ADD COLUMN notes TEXT;

CREATE TABLE order_item_addons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id UUID NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    addon_id UUID REFERENCES menu_addons(id) ON DELETE SET NULL,
    name_en VARCHAR(255) NOT NULL,
    name_ar VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_item_addons_item_id ON order_item_addons(order_item_id);
