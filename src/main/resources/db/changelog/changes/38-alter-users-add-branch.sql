-- liquibase formatted sql

-- changeset resapori:38-alter-users-add-branch
-- comment: Add branch_id foreign key to users table for employee branch association

ALTER TABLE users ADD COLUMN IF NOT EXISTS branch_id UUID REFERENCES branches(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_users_branch_id ON users(branch_id);
