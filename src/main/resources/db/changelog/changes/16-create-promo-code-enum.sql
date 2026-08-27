-- liquibase formatted sql

-- changeset resapori:16-create-promo-code-enum
-- comment: Add PostgreSQL ENUM type for promo code discount types

CREATE TYPE discount_type AS ENUM ('PERCENTAGE', 'FREE_ITEM');
