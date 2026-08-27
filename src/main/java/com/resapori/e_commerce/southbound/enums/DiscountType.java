package com.resapori.e_commerce.southbound.enums;

/**
 * Defines the type of discount a promo code grants.
 *
 * <ul>
 *   <li>{@link #PERCENTAGE} – reduces the order total by a percentage (0–100).</li>
 *   <li>{@link #FREE_ITEM}  – adds a specific {@code MenuItem} to the order at zero cost.</li>
 * </ul>
 */
public enum DiscountType {
    PERCENTAGE,
    FREE_ITEM
}
