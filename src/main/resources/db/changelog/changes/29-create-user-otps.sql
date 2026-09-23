-- liquibase formatted sql

-- changeset resapori:29-create-user-otps
-- comment: Create user_otps table for password reset OTP verification
-- validCheckSum: ANY

CREATE TABLE user_otps (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255)    NOT NULL,
    otp_code        VARCHAR(10)     NOT NULL,
    is_verified     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    expiry_date     TIMESTAMP       NOT NULL
);

CREATE INDEX idx_user_otps_email ON user_otps (email);
