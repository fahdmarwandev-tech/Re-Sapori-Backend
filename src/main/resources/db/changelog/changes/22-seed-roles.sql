-- liquibase formatted sql

-- changeset resapori:22-seed-roles
-- comment: Seed the application roles (idempotent). Role entity has only id and name columns.

INSERT INTO roles (id, name)
VALUES
    (gen_random_uuid(), 'CUSTOMER'),
    (gen_random_uuid(), 'ADMIN'),
    (gen_random_uuid(), 'CASHIER')
ON CONFLICT (name) DO NOTHING;
