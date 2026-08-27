-- liquibase formatted sql

-- changeset resapori:18-create-promo-code-redemptions
-- comment: Create the promo_code_redemptions table.
--          One row is written every time a promo code is successfully applied to an order.
--          Stores a snapshot of the monetary discount at the time of redemption for
--          auditing and reporting, independent of future price changes.

CREATE TABLE promo_code_redemptions (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    promo_code_id       UUID            NOT NULL,
    user_id             UUID            NOT NULL,
    order_id            UUID            NOT NULL,

    -- Monetary value of the discount applied (snapshot at redemption time)
    discount_applied    NUMERIC(10, 2)  NOT NULL,

    -- Timestamp of the redemption event
    redeemed_at         TIMESTAMP       NOT NULL DEFAULT NOW(),

    -- ── Constraints ──────────────────────────────────────────────────────────

    -- One redemption per order — a code cannot be applied twice to the same order
    CONSTRAINT uq_promo_redemptions_order
        UNIQUE (order_id),

    CONSTRAINT fk_promo_redemptions_promo_code
        FOREIGN KEY (promo_code_id)
        REFERENCES promo_codes (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_promo_redemptions_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_promo_redemptions_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE RESTRICT
);

-- Used to count how many times a specific user has redeemed a specific code
CREATE INDEX idx_promo_redemptions_code_user   ON promo_code_redemptions (promo_code_id, user_id);

-- Used to look up all redemptions for a given code (analytics / admin)
CREATE INDEX idx_promo_redemptions_promo_code  ON promo_code_redemptions (promo_code_id);

-- Used to look up all redemptions by a given user (history page)
CREATE INDEX idx_promo_redemptions_user_id     ON promo_code_redemptions (user_id);
