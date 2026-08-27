-- liquibase formatted sql

-- changeset resapori:17-create-promo-codes
-- comment: Create the promo_codes table.
--          A code is user-specific when user_id is set; otherwise it is public.
--          PERCENTAGE codes carry a discount_value (0–100).
--          FREE_ITEM codes reference a menu item via free_item_id.
--          Expiry and usage limits are all optional (NULL = unlimited).
--          max_uses_per_user defaults to 1 to prevent duplicate redemptions by the same user.

CREATE TABLE promo_codes (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    -- The promo string the customer enters at checkout
    code                VARCHAR(50)     NOT NULL,

    description_en      TEXT,
    description_ar      TEXT,

    -- Drives which discount columns are active
    discount_type       discount_type   NOT NULL,

    -- Populated when discount_type = 'PERCENTAGE' (CHECK enforces 0–100)
    discount_value      NUMERIC(5, 2),

    -- Populated when discount_type = 'FREE_ITEM'
    free_item_id        UUID,

    -- NULL = public code; non-NULL = only this user may redeem it
    user_id             UUID,

    -- NULL = no time limit
    expiry_date         TIMESTAMP,

    -- NULL = unlimited global redemptions
    max_uses            INT,

    -- Running counter incremented on every redemption
    current_uses        INT             NOT NULL DEFAULT 0,

    -- NULL = unlimited per-user redemptions; default 1 = one redemption per user
    max_uses_per_user   INT             NOT NULL DEFAULT 1,

    -- Inherited audit columns
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(255),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          VARCHAR(255),

    -- ── Constraints ──────────────────────────────────────────────────────────

    -- Code must be unique across the whole table
    CONSTRAINT uq_promo_codes_code
        UNIQUE (code),

    -- Percentage value must be between 0 and 100 when present
    CONSTRAINT chk_promo_codes_discount_value
        CHECK (discount_value IS NULL OR (discount_value >= 0 AND discount_value <= 100)),

    -- Exactly one of discount_value / free_item_id must be set depending on type
    CONSTRAINT chk_promo_codes_percentage_has_value
        CHECK (
            discount_type <> 'PERCENTAGE'
            OR (discount_value IS NOT NULL AND free_item_id IS NULL)
        ),
    CONSTRAINT chk_promo_codes_free_item_has_ref
        CHECK (
            discount_type <> 'FREE_ITEM'
            OR (free_item_id IS NOT NULL AND discount_value IS NULL)
        ),

    -- current_uses may never exceed max_uses when max_uses is set
    CONSTRAINT chk_promo_codes_uses_not_exceeded
        CHECK (max_uses IS NULL OR current_uses <= max_uses),

    CONSTRAINT fk_promo_codes_free_item
        FOREIGN KEY (free_item_id)
        REFERENCES menu_items (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_promo_codes_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- Fast lookup by code string (the most common query path)
CREATE UNIQUE INDEX idx_promo_codes_code     ON promo_codes (code);

-- Filter user-specific codes quickly
CREATE INDEX idx_promo_codes_user_id         ON promo_codes (user_id);

-- Support filtering by expiry_date for cleanup / validity checks
CREATE INDEX idx_promo_codes_expiry_date     ON promo_codes (expiry_date);
