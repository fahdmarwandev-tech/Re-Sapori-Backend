-- liquibase formatted sql

-- changeset resapori:20-alter-users-add-address
-- comment: Add address column to users table

ALTER TABLE users ADD COLUMN address VARCHAR(255);
