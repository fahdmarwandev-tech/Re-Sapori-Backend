# Re-Sapori Backend — Full Implementation Plan

> **Last updated:** 2026-08-29
> **Status:** In progress — Auth ✅ | User Addresses ✅ | Everything else 🔴 Not started

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Role & Access Model](#2-role--access-model)
3. [Implementation Phases](#3-implementation-phases)
4. [Phase 1 — Foundation Refactor](#phase-1--foundation-refactor)
5. [Phase 2 — Menu & Branches](#phase-2--menu--branches)
6. [Phase 3 — Orders](#phase-3--orders)
7. [Phase 4 — Payments (Paymob)](#phase-4--payments-paymob)
8. [Phase 5 — Promo Codes](#phase-5--promo-codes)
9. [Coding Rules Reference](#9-coding-rules-reference)

---

## 1. Architecture Overview

The backend follows a **layered hexagonal-style architecture**:

```
HTTP Request
     │
     ▼
┌─────────────────────────────┐
│  Northbound (Controllers)   │  ← Receives HTTP, delegates to service, returns DTOs
├─────────────────────────────┤
│  Service Layer              │  ← All business logic lives here
├─────────────────────────────┤
│  Southbound (Repositories)  │  ← JPA repositories, DB queries only
├─────────────────────────────┤
│  Database (PostgreSQL)      │  ← Schema managed by Liquibase
└─────────────────────────────┘
```

### Strict rules (see `coding-restrictions.md` for full details)
- Controllers **never** contain business logic
- Entities **never** leave the service layer — always map to DTOs
- **MapStruct** for all entity ↔ DTO mapping
- **No N+1 queries** — use `JOIN FETCH` or `findAllById` for bulk reads
- **1–2 DB calls max** per endpoint
- Functions **max 20 lines** — extract to private helpers otherwise

---

## 2. Role & Access Model

### Roles

| Role | Description |
|------|-------------|
| `CUSTOMER` | Default role assigned on registration. Can browse menu, place orders, manage own profile/addresses. |
| `ADMIN` | Full access. Manages menu, branches, orders, promo codes, users. |
| `CASHIER` | Manages in-store orders. Can update order status, view orders for their branch. |
| `GUEST` | Unauthenticated. Read-only access to menu. |

### Registration rule
> `POST /api/auth/register` always assigns the `CUSTOMER` role. Other roles are assigned by an admin through the role management API.

### Access Matrix

| Endpoint Group | GUEST | CUSTOMER | CASHIER | ADMIN |
|----------------|-------|----------|---------|-------|
| Browse menu (read) | ✅ | ✅ | ✅ | ✅ |
| Manage menu (write) | ❌ | ❌ | ❌ | ✅ |
| View branches | ✅ | ✅ | ✅ | ✅ |
| Manage branches | ❌ | ❌ | ❌ | ✅ |
| Place order | ❌ | ✅ | ✅ | ✅ |
| View own orders | ❌ | ✅ | ✅ | ✅ |
| View all orders | ❌ | ❌ | ✅ (branch) | ✅ |
| Update order status | ❌ | ❌ | ✅ | ✅ |
| Initiate payment | ❌ | ✅ | ✅ | ✅ |
| Manage promo codes | ❌ | ❌ | ❌ | ✅ |
| Apply promo code | ❌ | ✅ | ✅ | ✅ |
| Manage own addresses | ❌ | ✅ | ✅ | ✅ |
| Manage users/roles | ❌ | ❌ | ❌ | ✅ |

---

## 3. Implementation Phases

Phases are strictly ordered — each depends on the previous being complete.

```
Phase 1: Foundation  ──►  Phase 2: Menu  ──►  Phase 3: Orders  ──►  Phase 4: Payments  ──►  Phase 5: Promos
```

### Current state at a glance

| Module | Status | Notes |
|--------|--------|-------|
| Auth (register, login, refresh, logout) | ✅ Done | |
| User Addresses | ✅ Done | |
| Global error handling | ✅ Done | `ErrorResponse`, `ExceptionHandlers`, `ResourceNotFoundException` exist |
| MapStruct mapping | 🔴 Not started | `southbound/mapper/` is empty — needed before any other feature |
| Role auto-assignment on register | 🔴 Not started | Registration currently creates a user with no role |
| Menu Categories | 🔴 Not started | |
| Menu Items | 🔴 Not started | |
| Branches | 🔴 Not started | |
| Orders | 🔴 Not started | |
| Payments (Paymob) | 🔴 Not started | Waiting for API key |
| Promo Codes | 🔴 Not started | |

---

## Phase 1 — Foundation Refactor

> **Goal:** Get the infrastructure right before adding features. This is quick but critical.

### 1.1 — Assign CUSTOMER role on registration

**Problem:** `AuthServiceImpl.register()` creates a `User` with no roles. Spring Security's `CustomUserDetails` maps roles to authorities, so a freshly-registered user has no permissions.

**Fix in `AuthServiceImpl.register()`:**
```java
Role customerRole = roleRepository.findByName("CUSTOMER")
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CUSTOMER role not found"));
user.setRoles(Set.of(customerRole));
```

**Also:** The four roles (`CUSTOMER`, `ADMIN`, `CASHIER`, and a placeholder `GUEST`) must be seeded in the database. Add a Liquibase changeset:

```
22-seed-roles.sql
```

```sql
-- changeset resapori:22-seed-roles
INSERT INTO roles (id, name, is_active, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'CUSTOMER', true, NOW(), NOW()),
  (gen_random_uuid(), 'ADMIN',    true, NOW(), NOW()),
  (gen_random_uuid(), 'CASHIER',  true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;
```

### 1.2 — MapStruct mappers (one per domain object)

**Problem:** `southbound/mapper/` is empty. Every controller currently passes raw entity objects as `@RequestBody` and returns them as responses, violating the architecture rules.

Create a mapper interface for each entity that needs it:

| Mapper | Maps |
|--------|------|
| `MenuCategoryMapper` | `MenuCategory` ↔ `MenuCategoryRequest` / `MenuCategoryResponse` |
| `MenuItemMapper` | `MenuItem` ↔ `MenuItemRequest` / `MenuItemResponse` |
| `BranchMapper` | `Branch` ↔ `BranchRequest` / `BranchResponse` |
| `OrderMapper` | `Order` ↔ `PlaceOrderRequest` / `OrderResponse` |
| `UserMapper` | `User` ↔ `UserResponse` (for admin user management) |

All mapper interfaces go in `southbound/mapper/`, annotated `@Mapper(componentModel = "spring")`.

### 1.3 — `SecurityConfig` role rules

Update the `authorizeHttpRequests` block to enforce the access matrix:

```java
.authorizeHttpRequests(auth -> auth
    // Public
    .requestMatchers(HttpMethod.GET, "/api/menu/**", "/api/branches").permitAll()
    .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
    // Customer
    .requestMatchers("/api/orders/my", "/api/users/*/addresses/**").hasAnyRole("CUSTOMER", "ADMIN")
    // Cashier + Admin
    .requestMatchers(HttpMethod.PATCH, "/api/orders/*/status").hasAnyRole("CASHIER", "ADMIN")
    // Admin only
    .requestMatchers(HttpMethod.POST, "/api/menu/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.PUT, "/api/menu/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.DELETE, "/api/menu/**").hasRole("ADMIN")
    .requestMatchers("/api/promo-codes/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

---

## Phase 2 — Menu & Branches

> **Goal:** Admins can manage the menu. Customers and guests can browse it.

### 2.1 — Menu Categories

#### DTOs
**`MenuCategoryRequest`** (`northbound/dto/menu/`)
```
nameEn   (required)
nameAr   (optional)
displayOrder (optional)
```

**`MenuCategoryResponse`**
```
id, nameEn, nameAr, displayOrder, isActive
```

#### Endpoints — `MenuCategoryController` → `/api/menu/categories`

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/` | Public | List all active categories, ordered by `displayOrder` |
| `GET` | `/{id}` | Public | Get single category |
| `POST` | `/` | ADMIN | Create category |
| `PUT` | `/{id}` | ADMIN | Update category |
| `DELETE` | `/{id}` | ADMIN | Soft-delete (set `isActive = false`) |

#### Service logic — `MenuCategoryServiceImpl`
- `getAll`: `findAll()` filtered by `isActive = true`, sorted by `displayOrder`
- `create` / `update`: validate unique `nameEn`
- `delete`: set `isActive = false`, do NOT physically delete (preserves order history)

---

### 2.2 — Menu Items

#### DTOs
**`MenuItemRequest`** (`northbound/dto/menu/`)
```
categoryId  (required, UUID)
nameEn      (required)
nameAr      (optional)
descriptionEn (optional)
descriptionAr (optional)
currentPrice (required, BigDecimal > 0)
imageUrl    (optional)
isAvailable (optional, default true)
stock       (optional)
```

**`MenuItemResponse`**
```
id, categoryId, categoryNameEn, nameEn, nameAr,
descriptionEn, descriptionAr, currentPrice,
imageUrl, isAvailable, stock
```

#### Endpoints — `MenuItemController` → `/api/menu/items`

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/` | Public | List all available items |
| `GET` | `/{id}` | Public | Get single item |
| `GET` | `/by-category/{categoryId}` | Public | List items in a category |
| `POST` | `/` | ADMIN | Create item |
| `PUT` | `/{id}` | ADMIN | Update item |
| `DELETE` | `/{id}` | ADMIN | Soft-delete |
| `PATCH` | `/{id}/availability` | ADMIN | Toggle `isAvailable` |

#### Service logic — `MenuItemServiceImpl`
- `getAll`: returns only `isActive = true` AND `isAvailable = true` for customers; ADMIN sees all
- `getByCategory`: uses `IMenuItemRepository.findByCategoryId(categoryId)` — uses `JOIN FETCH category` to avoid N+1

---

### 2.3 — Branches

#### DTOs
**`BranchRequest`** / **`BranchResponse`** (`northbound/dto/branch/`)
```
BranchRequest:  name (required), address, phoneNumber
BranchResponse: id, name, address, phoneNumber, isActive
```

#### Endpoints — `BranchController` → `/api/branches`

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/` | Public | List all active branches |
| `GET` | `/{id}` | Public | Get single branch |
| `POST` | `/` | ADMIN | Create branch |
| `PUT` | `/{id}` | ADMIN | Update branch |
| `DELETE` | `/{id}` | ADMIN | Soft-delete |

> **Note:** The current `BranchController` maps to `/api/branchs` (typo). Change to `/api/branches`.

---

## Phase 3 — Orders

> **Goal:** Customers can place orders. Cashiers and admins can view and update them.

> **No cancellation** — once placed, an order cannot be cancelled by the customer.

### 3.1 — The "Place Order" flow

```
Customer POST /api/orders
    │
    ├── 1. Authenticate user (from JWT)
    ├── 2. Validate all menuItemIds exist and isAvailable = true
    ├── 3. Snapshot unit prices (menuItem.currentPrice → orderItem.unitPriceAtPurchase)
    ├── 4. Calculate subtotal = Σ (quantity × unitPriceAtPurchase)
    ├── 5. If promoCode provided → validate & apply (see Phase 5)
    ├── 6. Set deliveryAddress (snapshot text from user's chosen address if DELIVERY)
    ├── 7. Save Order (status = PENDING)
    └── 8. Save OrderItems (batch)
```

### 3.2 — DTOs

**`PlaceOrderRequest`** (`northbound/dto/order/`)
```
orderType       (required — DELIVERY | PICKUP | DINE_IN)
branchId        (required for PICKUP and DINE_IN)
addressId       (required for DELIVERY — UUID of user's saved address)
items           List<OrderItemInput>:
    menuItemId  (required)
    quantity    (required, min 1)
promoCode       (optional string)
paymentMethod   (required — CASH_ON_DELIVERY | CARD | WALLET | KIOSK)
```

**`OrderItemResponse`**
```
menuItemId, nameEn, nameAr, quantity, unitPriceAtPurchase, lineTotal
```

**`OrderResponse`**
```
id, status, orderType, paymentMethod,
totalAmount, currency, deliveryAddress,
branch { id, name },
items [ OrderItemResponse ],
createdAt
```

**`UpdateOrderStatusRequest`**
```
status  (OrderStatus enum value)
```

### 3.3 — Endpoints — `OrderController` → `/api/orders`

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `POST` | `/` | CUSTOMER, CASHIER | Place a new order |
| `GET` | `/my` | CUSTOMER | Get authenticated user's own orders |
| `GET` | `/{id}` | CUSTOMER (own), CASHIER, ADMIN | Get order details |
| `GET` | `/` | ADMIN, CASHIER | Get all orders (with pagination) |
| `PATCH` | `/{id}/status` | CASHIER, ADMIN | Update order status |

### 3.4 — Order status lifecycle

```
PENDING ──► PAYMENT_PENDING ──► PAID ──► PREPARING ──► READY ──► DELIVERED
                           └──► PAYMENT_FAILED
```

- `PENDING` → set immediately on creation (cash orders skip straight to `PREPARING` after cashier confirms)
- `PAYMENT_PENDING` → set when Paymob checkout is initiated (Phase 4)
- `PAID` → set by Paymob webhook callback (Phase 4)
- `PAYMENT_FAILED` → set by Paymob webhook on failure (Phase 4)
- `PREPARING`, `READY`, `DELIVERED` → set manually by CASHIER or ADMIN

### 3.5 — Service logic highlights — `OrderServiceImpl`

- **Batch fetch menu items:** `menuItemRepository.findAllById(itemIds)` — single query, never loop
- **Price snapshot:** Always snapshot at order time, never recalculate
- **Delivery address snapshot:** Copy the text from `UserAddress` into `Order.deliveryAddress` — if the user later edits/deletes the address, the order history is preserved
- **Ownership check on GET:** A `CUSTOMER` can only fetch their own orders (compare `order.user.id` with authenticated user id)

---

## Phase 4 — Payments (Paymob)

> **Note:** Paymob API key, integration ID, and HMAC secret are not yet available. This phase starts when credentials are provided.

### 4.1 — Payment flow

```
1. Customer POST /api/orders/{id}/pay
       │
       ├── Validate order belongs to user and status = PENDING
       ├── Create Payment record (status = PENDING)
       ├── Call Paymob "Create Intention" API → get payment_key & checkout_url
       ├── Store paymobOrderId on Order
       ├── Update Order status → PAYMENT_PENDING
       └── Return { checkoutUrl } to frontend

2. Customer completes payment on Paymob's hosted page

3. Paymob POST /api/payments/callback  (public endpoint — no auth required)
       │
       ├── Verify HMAC signature (reject if invalid)
       ├── Parse transaction result
       ├── Find Payment by paymobTransactionId
       ├── On SUCCESS:
       │     ├── Update Payment.status → SUCCESS
       │     └── Update Order.status → PAID
       └── On FAILURE:
             ├── Update Payment.status → FAILED
             └── Update Order.status → PAYMENT_FAILED
```

### 4.2 — Environment variables (add to `.env`)

```
PAYMOB_API_KEY=
PAYMOB_INTEGRATION_ID=
PAYMOB_HMAC_SECRET=
```

### 4.3 — New components

| Component | Location | Purpose |
|-----------|----------|---------|
| `PaymobClient` | `common/client/` | Wraps all HTTP calls to Paymob API using Spring `RestClient` |
| `PaymobProperties` | `common/config/` | `@ConfigurationProperties` binding for the three env vars |
| `PaymobCallbackRequest` | `northbound/dto/payment/` | Maps the Paymob webhook JSON body |
| `InitiatePaymentResponse` | `northbound/dto/payment/` | Returns `{ checkoutUrl, paymobOrderId }` to frontend |

### 4.4 — Endpoints

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `POST` | `/api/orders/{id}/pay` | CUSTOMER (own order) | Initiate Paymob checkout, get checkout URL |
| `POST` | `/api/payments/callback` | Public (Paymob webhook) | Receive callback, verify HMAC, update statuses |
| `GET` | `/api/orders/{id}/payments` | ADMIN | View payment history for an order |

---

## Phase 5 — Promo Codes

> **Reference:** See `docs/promo-codes.md` for the full promo code system design, validation rules, and data model.

### 5.1 — Admin side (CRUD)

**`PromoCodeRequest`** (`northbound/dto/promo/`)
```
code            (required, unique, max 50 chars)
descriptionEn   (optional)
descriptionAr   (optional)
discountType    (required — PERCENTAGE | FREE_ITEM)
discountValue   (required if PERCENTAGE, 0–100)
freeItemId      (required if FREE_ITEM)
userId          (optional — null = public code)
expiryDate      (optional)
maxUses         (optional — null = unlimited)
maxUsesPerUser  (default 1)
```

**Endpoints** → `/api/promo-codes` (ADMIN only)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/` | Create promo code |
| `GET` | `/` | List all codes |
| `GET` | `/{id}` | Get single code with usage stats |
| `PUT` | `/{id}` | Update code |
| `DELETE` | `/{id}` | Soft-delete (set `isActive = false`) |

### 5.2 — Customer side (apply at checkout)

Promo code application is embedded inside the `PlaceOrderRequest` (see Phase 3). The `OrderServiceImpl` delegates promo validation to `PromoCodeServiceImpl`.

**Validation sequence** (all 5 must pass — fail fast):
1. Code exists and `isActive = true`
2. Not expired (`expiryDate == null || expiryDate.isAfter(now())`)
3. Global cap not reached (`maxUses == null || currentUses < maxUses`)
4. User is eligible (`promoCode.userId == null || promoCode.userId == requestingUserId`)
5. Per-user cap not reached (`countByPromoCodeIdAndUserId < maxUsesPerUser`)

**On success** (all within a single `@Transactional`):
- `PERCENTAGE`: `discount = total × (value / 100)` → subtract from `Order.totalAmount`
- `FREE_ITEM`: add an extra `OrderItem` with `unitPriceAtPurchase = 0`
- Save `PromoCodeRedemption` row
- Call `IPromoCodeRepository.incrementCurrentUses(id)`

---

## 9. Coding Rules Reference

A quick summary — full rules in [`coding-restrictions.md`](coding-restrictions.md).

| Rule | Summary |
|------|---------|
| DTO-only API | Never expose JPA entities outside the service layer |
| MapStruct | All mapping goes through `@Mapper` interfaces in `southbound/mapper/` |
| No business logic in controllers | Controllers: receive → validate → delegate → return |
| Max 20 lines per function | Split into private helpers |
| 1–2 DB calls per endpoint | Use `JOIN FETCH`, `findAllById`, bulk queries |
| No DB calls in loops | Never call a repository inside `for` / `while` |
| `@Transactional` on mutations | All service methods that write to the DB must be `@Transactional` |
| Constructor injection | Always via Lombok `@RequiredArgsConstructor` |
| Soft-deletes | Set `isActive = false` — never hard-delete from application code |
| Error responses | Always through `ExceptionHandlers` — throw `ResponseStatusException` or `ResourceNotFoundException` |
