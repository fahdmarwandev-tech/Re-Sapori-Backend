-- liquibase formatted sql

-- changeset resapori:13-create-payment-enums
-- comment: Add PostgreSQL ENUM types for order type, payment method, and payment status

CREATE TYPE order_type AS ENUM ('DELIVERY', 'PICKUP', 'DINE_IN');

CREATE TYPE payment_method AS ENUM ('CASH_ON_DELIVERY', 'CARD', 'WALLET', 'KIOSK');

CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED', 'VOIDED');
