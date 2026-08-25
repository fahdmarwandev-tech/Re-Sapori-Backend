-- liquibase formatted sql

-- changeset resapori:1-create-enums
-- comment: Create custom PostgreSQL ENUM types for user roles and order statuses

CREATE TYPE user_role AS ENUM ('customer', 'admin', 'kitchen_staff');

CREATE TYPE order_status AS ENUM ('pending', 'paid', 'preparing', 'ready', 'delivered', 'cancelled');
