# Re-Sapori Offers & Promotions — Frontend Integration Guide

This document outlines the complete architectural flow, API contracts, TypeScript definitions, client-side pricing logic, and checkout payloads for the **Offers & Promotions Module** in Re-Sapori.

---

## 1. Overview of Supported Offer Types

The system provides a single, unified data model that supports all promotional and combo mechanics:

| Offer Mechanic | Example | `discountTarget` | How Pricing Operates |
|---|---|:---:|---|
| **Pay for Most Expensive** | "Buy 2 Pizzas, Pay for the Highest" | `CHEAPEST_ITEM` | Top 1 item full price, 2nd item 100% off (Free). |
| **Buy X Get Y Free (BOGO)** | "Buy 2 get 1 Free", "Buy 3 get 1 Free" | `CHEAPEST_ITEM` | Top $X$ items full price, bottom $Y$ cheapest items 100% off. |
| **Buy X Get Y Discounted** | "Buy 2 get 3rd at 50% Off" | `CHEAPEST_ITEM` | Top $X$ items full price, $Y$ cheapest item gets 50% off. |
| **Total Bundle Discount** | "Buy 2 get 10% off the total" | `TOTAL_BUNDLE` | Sum of all selected items $\times (1 - \text{discountPercentage}/100)$. |
| **Fixed Price Combo Meal** | "Kids Meal for 150 EGP", "Family Deal 550 EGP" | `FIXED_PRICE` | Charges flat `fixedPrice` regardless of item component prices. |
| **Complimentary / Free Items** | "Buy 2 Pizzas + FREE Pepsi + 2 FREE Sauces" | Any | Items flagged `isFree = true` are always **0.00 EGP** ($100\%$ free). |
| **Selected Items Whitelist** | "Only on Classic Pizzas (excluding Truffle/Seafood)" | Any | Dynamic slots filter strictly to `eligibleItems`. |

---

## 2. API Endpoints

### 2.1 Public Endpoints (Customer Facing)

#### `GET /api/offers`
Returns all active offers for the menu / landing page.
- **Query Params:** `?category={categoryId}` (optional filter)
- **Response:** `OfferResponse[]`

#### `GET /api/offers/{id}`
Returns full details of an offer, including its slots, pre-included items, and eligible selection lists.
- **Response:** `OfferResponse`

---

## 3. TypeScript Interfaces & DTOs

Add these types to `lib/api.ts` (or `types/offers.ts`):

```typescript
// ─── Enums ─────────────────────────────────────────────────────────────────────

export type DiscountTarget = 'TOTAL_BUNDLE' | 'CHEAPEST_ITEM' | 'FIXED_PRICE';

// ─── Component / Slot DTO ─────────────────────────────────────────────────────

export interface MenuItemSummary {
  id: string;
  nameEn: string;
  nameAr: string;
  currentPrice: number;
  originalPrice?: number | null;
  discountPrice?: number | null;
  imageUrl?: string;
  isAvailable: boolean;
}

export interface OfferSlotResponse {
  id: string;
  slotNameEn: string;
  slotNameAr: string;
  
  /**
   * If true, this item is a complimentary gift (e.g. Free Pepsi, Free Sauce)
   * and is ALWAYS charged at 0.00 EGP.
   */
  isFree: boolean;
  
  /** Number of items to pick for this slot (default 1) */
  quantity: number;
  displayOrder: number;

  /**
   * STATIC ITEM:
   * If set, this exact item is automatically included in the offer.
   * Customer cannot change it (e.g., fixed 1L Pepsi).
   */
  fixedItem?: MenuItemSummary | null;

  /**
   * DYNAMIC SELECTION:
   * If set, the customer chooses from this category or eligibleItems list.
   */
  eligibleCategoryId?: string | null;
  eligibleCategoryNameEn?: string | null;
  eligibleCategoryNameAr?: string | null;

  /**
   * WHITELIST (Selected Items Only):
   * When populated, customer can ONLY pick from this specific array of items.
   * If empty/null, customer can pick ANY active item from eligibleCategoryId.
   */
  eligibleItems?: MenuItemSummary[];
}

// ─── Master Offer DTO ─────────────────────────────────────────────────────────

export interface OfferResponse {
  id: string;
  nameEn: string;
  nameAr: string;
  descriptionEn?: string;
  descriptionAr?: string;
  imageUrl?: string;

  discountTarget: DiscountTarget;

  /** Percentage discount (e.g. 10.00 for 10% off, 50.00 for 50% off, 100.00 for FREE) */
  discountPercentage?: number | null;

  /** Flat price in EGP (used when discountTarget === 'FIXED_PRICE') */
  fixedPrice?: number | null;

  /** Number of items charged full price (e.g., 2 in Buy 2 Get 1) */
  buyQuantity: number;

  /** Number of items discounted (e.g., 1 in Buy 2 Get 1) */
  getQuantity: number;

  /** Base category (optional metadata) */
  categoryId?: string | null;
  categoryNameEn?: string | null;
  categoryNameAr?: string | null;

  /** List of components/slots in the offer */
  slots: OfferSlotResponse[];

  isActive: boolean;
  createdAt: string;
}
```

---

## 4. Client-Side Pricing Calculation Logic

Use this helper function on the frontend to calculate the live price inside the offer customizer modal as the customer picks items:

```typescript
interface SelectedSlotItem {
  slotId: string;
  menuItem: MenuItemSummary;
  quantity?: number; // default 1
  size?: 'REGULAR' | 'MINI'; // default REGULAR
  addOnsTotal?: number; // Sum of any selected add-ons (extra cheese, burrata, etc.)
  isFree: boolean;
}

export function calculateOfferPrice(
  offer: OfferResponse,
  selections: SelectedSlotItem[]
): {
  finalPrice: number;
  originalPrice: number;
  totalSavings: number;
} {
  // Flatten items by quantity to handle slots where quantity > 1 (e.g. 2 sauces)
  const flattened: SelectedSlotItem[] = [];
  selections.forEach((s) => {
    const qty = s.quantity && s.quantity > 1 ? s.quantity : 1;
    for (let i = 0; i < qty; i++) {
      flattened.push({ ...s, quantity: 1, addOnsTotal: i === 0 ? s.addOnsTotal : 0 });
    }
  });

  // Calculate add-ons sum (add-ons are always billed on top of base deal)
  const totalAddOns = selections.reduce((sum, s) => sum + (s.addOnsTotal ?? 0), 0);

  // 1. Calculate original total (what it would cost without any offer)
  const baseOriginal = flattened.reduce((sum, s) => sum + s.menuItem.currentPrice, 0);
  const originalPrice = baseOriginal + totalAddOns;

  // 2. Fixed Price Combos
  if (offer.discountTarget === 'FIXED_PRICE') {
    const finalPrice = (offer.fixedPrice ?? 0) + totalAddOns;
    return {
      finalPrice,
      originalPrice,
      totalSavings: Math.max(0, originalPrice - finalPrice),
    };
  }

  // Separate free items from paid items
  const paidItems = flattened.filter((s) => !s.isFree);

  // Sort paid items descending by current price
  const sortedPaidPrices = paidItems
    .map((s) => s.menuItem.currentPrice)
    .sort((a, b) => b - a);

  let baseFinalPrice = 0;

  // 3. Discount on Entire Bundle (e.g. Buy 2 get 10% off total)
  if (offer.discountTarget === 'TOTAL_BUNDLE') {
    const rawSum = sortedPaidPrices.reduce((sum, p) => sum + p, 0);
    const discountPct = (offer.discountPercentage ?? 0) / 100;
    baseFinalPrice = Math.round(rawSum * (1 - discountPct));
  }

  // 4. Discount on Cheapest Item(s) (e.g. BOGO, Pay Highest, 3rd 50% off)
  else if (offer.discountTarget === 'CHEAPEST_ITEM') {
    const buyQty = offer.buyQuantity;
    const getQty = offer.getQuantity;
    const discountPct = (offer.discountPercentage ?? 100) / 100;

    // Top buyQuantity items charged at full price
    const fullPriceSum = sortedPaidPrices
      .slice(0, buyQty)
      .reduce((sum, p) => sum + p, 0);

    // Next getQuantity items receive the discount
    const discountedSum = sortedPaidPrices
      .slice(buyQty, buyQty + getQty)
      .reduce((sum, p) => sum + Math.round(p * (1 - discountPct)), 0);

    // Excess items beyond buyQty + getQty charged at full price (prevents over-selection leak)
    const excessFullPriceSum = sortedPaidPrices
      .slice(buyQty + getQty)
      .reduce((sum, p) => sum + p, 0);

    baseFinalPrice = fullPriceSum + discountedSum + excessFullPriceSum;
  }

  const finalPrice = baseFinalPrice + totalAddOns;

  return {
    finalPrice,
    originalPrice,
    totalSavings: Math.max(0, originalPrice - finalPrice),
  };
}
```

---

## 5. UI/UX Recommendations for the Frontend

### 5.1 Menu Card Badge Display
- **Complimentary Items:** Display a green badge: `+ FREE 1L Pepsi & 2 Sauces` (`+ بيبسي وصوصات مجاناً`).
- **Discount Badges:**
  - `TOTAL_BUNDLE`: `10% OFF TOTAL` (`خصم ١٠٪ على الإجمالي`).
  - `CHEAPEST_ITEM` with 100%: `BUY 2 PAY 1` (`اشتر ٢ وادفع لواحد`).
  - `CHEAPEST_ITEM` with 50%: `3RD ITEM 50% OFF` (`القطعة الثالثة بنصف السعر`).
  - `FIXED_PRICE`: Strike-through calculated original price vs `fixedPrice`.

### 5.2 Offer Customizer Modal
1. **Pre-included Free Items (`isFree = true` and `fixedItem != null`):**
   - Render as a locked card with a green `FREE` tag.
   - Example: `[✓] 1x Pepsi 1 Liter (0.00 EGP - FREE)`.
2. **Dynamic Choice Slots (`eligibleCategoryId != null`):**
   - If `eligibleItems` has elements, only display those options in the picker.
   - If `isFree = true`, show `0.00 EGP` on all options in that slot (e.g., choice of free sauces).
3. **Live Price Bar:**
   - Shows live updated total, strike-through original sum, and savings badge: `Save 240 EGP!`.

---

## 6. Cart State Structure

In your frontend `CartContext`, store offer items with their selections:

```typescript
export interface CartOfferSelection {
  slotId: string;
  slotName: string;
  menuItemId: string;
  menuItemName: string;
  unitPrice: number;
  isFree: boolean;
}

export interface CartOfferItem {
  cartItemId: string; // Unique cart row ID (e.g. `${offerId}-${Date.now()}`)
  type: 'OFFER';
  offerId: string;
  offerName: string;
  quantity: number;
  totalPrice: number; // Unit offer price * quantity
  selections: CartOfferSelection[];
}
```

---

## 7. Order Placement API Request (Checkout)

When the customer submits their order, send the chosen items inside the `offers` array of `PlaceOrderRequest`:

### Endpoint: `POST /api/orders`

```json
{
  "orderType": "DELIVERY",
  "paymentMethod": "CASH_ON_DELIVERY",
  "addressId": "8f8b8cf8-3482-4115-b286-905183363364",
  "promoCode": "SAPORI10",
  "items": [
    {
      "menuItemId": "d0000000-0000-0000-0000-000000000001",
      "quantity": 1,
      "size": "REGULAR"
    }
  ],
  "offers": [
    {
      "offerId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "quantity": 1,
      "selections": [
        {
          "slotId": "b1b2c3d4-0001-0000-0000-000000000001",
          "menuItemId": "d0000000-0000-0000-0000-000000000002",
          "quantity": 1,
          "size": "REGULAR",
          "addOnIds": ["d0000000-0000-0000-0000-000000000099"]
        },
        {
          "slotId": "b1b2c3d4-0002-0000-0000-000000000002",
          "menuItemId": "d0000000-0000-0000-0000-000000000003",
          "quantity": 1,
          "size": "REGULAR"
        },
        {
          "slotId": "b1b2c3d4-0003-0000-0000-000000000003",
          "menuItemId": "d0000000-0000-0000-0000-000000000015",
          "quantity": 1,
          "size": "REGULAR"
        },
        {
          "slotId": "b1b2c3d4-0004-0000-0000-000000000004",
          "menuItemId": "d0000000-0000-0000-0000-000000000020",
          "quantity": 2,
          "size": "REGULAR"
        }
      ]
    }
  ]
}
```

---

## 8. Order Details & Kitchen Fulfillment Response

In `OrderResponse`, items belonging to an offer are grouped together via `offerId` and `bundleGroupId`:

```json
{
  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "status": "PENDING",
  "totalAmount": 720.00,
  "currency": "EGP",
  "items": [
    {
      "id": "e1111111-0000-0000-0000-000000000001",
      "menuItemId": "d0000000-0000-0000-0000-000000000002",
      "nameEn": "Chicken Pesto Pizza",
      "nameAr": "دجاج بيستو",
      "quantity": 1,
      "size": "REGULAR",
      "unitPriceAtPurchase": 480.00,
      "lineTotal": 480.00,
      "offerId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "offerName": "Weekend Pizza Deal",
      "isFree": false
    },
    {
      "id": "e1111111-0000-0000-0000-000000000002",
      "menuItemId": "d0000000-0000-0000-0000-000000000003",
      "nameEn": "Margherita Pizza",
      "nameAr": "مارجريتا",
      "quantity": 1,
      "size": "REGULAR",
      "unitPriceAtPurchase": 240.00,
      "lineTotal": 240.00,
      "offerId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "offerName": "Weekend Pizza Deal",
      "isFree": false
    },
    {
      "id": "e1111111-0000-0000-0000-000000000003",
      "menuItemId": "d0000000-0000-0000-0000-000000000015",
      "nameEn": "Pepsi 1 Liter",
      "nameAr": "بيبسي ١ لتر",
      "quantity": 1,
      "size": "REGULAR",
      "unitPriceAtPurchase": 0.00,
      "lineTotal": 0.00,
      "offerId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "offerName": "Weekend Pizza Deal",
      "isFree": true
    },
    {
      "id": "e1111111-0000-0000-0000-000000000004",
      "menuItemId": "d0000000-0000-0000-0000-000000000020",
      "nameEn": "Garlic Dip",
      "nameAr": "صوص ثوم",
      "quantity": 2,
      "size": "REGULAR",
      "unitPriceAtPurchase": 0.00,
      "lineTotal": 0.00,
      "offerId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "offerName": "Weekend Pizza Deal",
      "isFree": true
    }
  ]
}
```

### Why this structure is ideal for the frontend:
1. **Invoice / History Screen:** Render grouped items with clear badges so the customer sees exact savings and free items.
2. **Kitchen Tickets & Printing:** The kitchen gets every single item, ensuring no sauces or drinks are forgotten.
3. **Inventory Consistency:** Free items properly deduct from store stock without throwing off balance sheets.
