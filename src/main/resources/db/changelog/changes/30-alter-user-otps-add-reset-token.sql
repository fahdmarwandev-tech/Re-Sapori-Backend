-- liquibase formatted sql

-- changeset resapori:30-alter-user-otps-add-reset-token
-- comment: Add reset_token and reset_token_expiry to user_otps table

ALTER TABLE user_otps ADD COLUMN reset_token VARCHAR(255);
ALTER TABLE user_otps ADD COLUMN reset_token_expiry TIMESTAMP;

CREATE INDEX idx_user_otps_reset_token ON user_otps (reset_token);
