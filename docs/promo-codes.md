# Promo Codes Module — Flow & Technical Reference

## Overview

The promo code system allows admins to create promotional discounts that customers apply at checkout. It is built around two database tables and two JPA entities: **`promo_codes`** (the code definition) and **`promo_code_redemptions`** (an immutable audit record per use).

---

## Table Summaries

### `promo_codes`

Holds every created promo code with all its rules and limits.

| Column | Purpose |
|---|---|
| `code` | The string the customer enters (e.g. `SUMMER20`). Unique across the table. |
| `discount_type` | `PERCENTAGE` or `FREE_ITEM`. Drives which discount columns are populated. |
| `discount_value` | % off order total (0–100). Only set when `discount_type = PERCENTAGE`. |
| `free_item_id` | FK → `menu_items`. Only set when `discount_type = FREE_ITEM`. |
| `user_id` | FK → `users`. **NULL = public code** (any user). Non-null = only that user may redeem it. |
| `expiry_date` | Hard cutoff timestamp. NULL = no date limit. |
| `max_uses` | Global cap on total redemptions. NULL = unlimited. |
| `current_uses` | Running counter. Incremented atomically on each redemption. |
| `max_uses_per_user` | Per-user redemption cap. **Defaults to 1**. |
| `is_active` | Manual disable flag (inherited from `BaseEntity`). |

> **Constraint guarantees (enforced at DB level)**
> - `discount_value` is always between 0 and 100 (`CHECK` constraint).
> - A `PERCENTAGE` code always has `discount_value` set and `free_item_id` null.
> - A `FREE_ITEM` code always has `free_item_id` set and `discount_value` null.
> - `current_uses` never exceeds `max_uses`.

---

### `promo_code_redemptions`

Immutable audit log. One row per successful redemption.

| Column | Purpose |
|---|---|
| `promo_code_id` | FK → `promo_codes`. |
| `user_id` | FK → `users` — who redeemed it. |
| `order_id` | FK → `orders` — which order it was applied to. UNIQUE (one code per order). |
| `discount_applied` | Monetary snapshot of the discount **at time of use** (price-change safe). |
| `redeemed_at` | Timestamp of redemption. |

---

## Entity Relationship Diagram

```
+----------+       +-----------------+       +---------------------------+
|  users   |------>|   promo_codes   |<------|   promo_code_redemptions  |
+----------+  0..1 |                 |  0..* +---------------------------+
                   |  user_id (opt.) |         | promo_code_id            |
                   |  free_item_id   |         | user_id                  |
                   +--------+--------+         | order_id (UNIQUE)        |
                            | 0..1             +-----------+--------------+
                       +----v------+                       |
                       |menu_items |               +-------v------+
                       +-----------+               |    orders    |
                                                   +--------------+
```

---

## Discount Type Logic

### `PERCENTAGE`

The service calculates:

```
discountAmount = orderTotal x (discountValue / 100)
newTotal       = orderTotal - discountAmount
```

`discountApplied` stored in `promo_code_redemptions` = `discountAmount`.

### `FREE_ITEM`

The service:
1. Fetches the `MenuItem` referenced by `free_item_id`.
2. Auto-creates an `OrderItem` row for that item with `unit_price = 0`.
3. Stores the item's **current market price** as `discountApplied` (snapshot).

---

## Validation Rules (Service Layer)

The service must run all five checks before allowing a redemption:

| # | Check | How |
|---|---|---|
| 1 | **Code exists & is active** | `findByCodeAndIsActiveTrue(code)` returns non-empty |
| 2 | **Not expired** | `expiryDate == null OR expiryDate.isAfter(now())` |
| 3 | **Global cap not reached** | `maxUses == null OR currentUses < maxUses` |
| 4 | **User is eligible** | `userId == null OR userId.equals(requestingUserId)` |
| 5 | **Per-user cap not reached** | `countByPromoCodeIdAndUserId(id, userId) < maxUsesPerUser` |

If all checks pass, the service must (within a single `@Transactional` method):
1. Save a `PromoCodeRedemption` row.
2. Call `IPromoCodeRepository.incrementCurrentUses(id)` to atomically bump the counter.

---

## Repository Reference

### `IPromoCodeRepository`

| Method | Purpose |
|---|---|
| `findByCodeAndIsActiveTrue(code)` | Main lookup — validates a code at checkout |
| `incrementCurrentUses(id)` | `@Modifying` JPQL — atomically bumps `current_uses` |

### `IPromoCodeRedemptionRepository`

| Method | Purpose |
|---|---|
| `countByPromoCodeIdAndUserId(...)` | Enforces `maxUsesPerUser` |
| `findByPromoCodeId(id)` | Admin analytics — all uses of a code |
| `findByUserId(userId)` | Customer history — all codes a user has used |
| `findByOrderId(orderId)` | Duplicate-apply guard |

---

## Liquibase Migration Files

| File | What it does |
|---|---|
| `16-create-promo-code-enum.sql` | Creates PostgreSQL `discount_type` ENUM |
| `17-create-promo-codes.sql` | Creates `promo_codes` table with all constraints & indexes |
| `18-create-promo-code-redemptions.sql` | Creates `promo_code_redemptions` table with FK and indexes |

---

## Design Decisions

| Decision | Rationale |
|---|---|
| `PromoCodeRedemption` does NOT extend `BaseEntity` | It is an immutable audit record; `is_active`, `updated_by`, `created_by` are irrelevant and would add noise. |
| `current_uses` counter on `promo_codes` | Allows a fast O(1) global-cap check without a `COUNT(*)` on the redemptions table on every checkout. |
| `discount_applied` snapshot | Freezes the discount value at redemption time so future code edits do not mutate historical records. |
| `max_uses_per_user` defaults to 1 | Sane default; prevents trivial abuse without extra config. Can be raised to NULL for public campaigns. |
| No FK from `orders` to `promo_codes` | The `promo_code_redemptions.order_id` column (with UNIQUE constraint) already gives a 1-to-1 mapping from order to code without adding a nullable FK to the `orders` table. |
