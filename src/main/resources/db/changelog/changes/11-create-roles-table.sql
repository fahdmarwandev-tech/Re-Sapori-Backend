-- liquibase formatted sql

-- changeset resapori:11-create-roles-table
-- comment: Create roles and user_roles tables

CREATE TABLE roles (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255)    NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id         UUID            NOT NULL,
    role_id         UUID            NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Note: In a production DB, you would typically copy data from the old enum to the new table here.
-- Since this is a fresh project, we just drop the old column and enum.
ALTER TABLE users DROP COLUMN role;
DROP TYPE user_role;
