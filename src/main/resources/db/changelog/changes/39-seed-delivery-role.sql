-- liquibase formatted sql

-- changeset resapori:39-seed-delivery-role
-- comment: Seed the DELIVERY role into roles table (idempotent)

INSERT INTO roles (id, name)
VALUES (gen_random_uuid(), 'DELIVERY')
ON CONFLICT (name) DO NOTHING;
