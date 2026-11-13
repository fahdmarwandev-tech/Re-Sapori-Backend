-- liquibase formatted sql

-- changeset resapori:46-allow-reuse-inactive-promo-codes
-- comment: Replace global uniqueness on promo_codes.code with a partial unique index for active codes only so soft-deleted (is_active = FALSE) codes can be recreated with the same code string.

ALTER TABLE promo_codes
    DROP CONSTRAINT IF EXISTS uq_promo_codes_code;

DROP INDEX IF EXISTS idx_promo_codes_code;

CREATE UNIQUE INDEX IF NOT EXISTS uq_promo_codes_active_code
    ON promo_codes (UPPER(code))
    WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_promo_codes_code
    ON promo_codes (code);
