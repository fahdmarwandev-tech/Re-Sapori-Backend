-- liquibase formatted sql

-- changeset resapori:43-expand-address-and-order-columns
-- comment: Expand user_addresses table with building, landmark, phone, car fields, and expand orders with structured delivery/car pickup columns

ALTER TABLE user_addresses ALTER COLUMN street DROP NOT NULL;
ALTER TABLE user_addresses ALTER COLUMN city DROP NOT NULL;

ALTER TABLE user_addresses
    ADD COLUMN IF NOT EXISTS address_type VARCHAR(50) DEFAULT 'DELIVERY',
    ADD COLUMN IF NOT EXISTS building VARCHAR(100),
    ADD COLUMN IF NOT EXISTS landmark VARCHAR(255),
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS car_plate VARCHAR(50),
    ADD COLUMN IF NOT EXISTS car_details VARCHAR(255);

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS address_id UUID REFERENCES user_addresses(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(50),
    ADD COLUMN IF NOT EXISTS street VARCHAR(255),
    ADD COLUMN IF NOT EXISTS building VARCHAR(100),
    ADD COLUMN IF NOT EXISTS floor VARCHAR(50),
    ADD COLUMN IF NOT EXISTS apartment VARCHAR(50),
    ADD COLUMN IF NOT EXISTS district VARCHAR(100),
    ADD COLUMN IF NOT EXISTS city VARCHAR(100),
    ADD COLUMN IF NOT EXISTS landmark VARCHAR(255),
    ADD COLUMN IF NOT EXISTS car_plate VARCHAR(50),
    ADD COLUMN IF NOT EXISTS car_details VARCHAR(255),
    ADD COLUMN IF NOT EXISTS lat DECIMAL(10, 7),
    ADD COLUMN IF NOT EXISTS lng DECIMAL(10, 7),
    ADD COLUMN IF NOT EXISTS google_maps_url VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_orders_address_id ON orders(address_id);
CREATE INDEX IF NOT EXISTS idx_user_addresses_address_type ON user_addresses(address_type);
