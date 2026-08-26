-- liquibase formatted sql

-- changeset resapori:12-alter-users-split-fullname
-- comment: Split full_name into first_name and last_name for Paymob billing_data compatibility

ALTER TABLE users
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name  VARCHAR(255);

-- Migrate existing data: put everything into first_name, leave last_name empty
UPDATE users SET first_name = full_name, last_name = '';

-- Now enforce NOT NULL
ALTER TABLE users
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name  SET NOT NULL;

ALTER TABLE users DROP COLUMN full_name;
