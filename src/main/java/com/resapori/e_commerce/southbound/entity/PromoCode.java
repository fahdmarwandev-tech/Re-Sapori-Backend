package com.resapori.e_commerce.southbound.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.resapori.e_commerce.southbound.enums.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a promotional code that can be applied at checkout.
 *
 * <p>A promo code is either <b>user-specific</b> (the {@code user} field is set and only
 * that user may redeem it) or <b>public</b> ({@code user} is {@code null} and any
 * authenticated user may redeem it).</p>
 *
 * <p>Discount variants:
 * <ul>
 *   <li>{@link DiscountType#PERCENTAGE} – {@code discountValue} holds a percentage (0–100);
 *       {@code freeItem} must be {@code null}.</li>
 *   <li>{@link DiscountType#FREE_ITEM} – {@code freeItem} holds the {@link MenuItem} to add
 *       at zero cost; {@code discountValue} must be {@code null}.</li>
 * </ul>
 *
 * <p>Usage limits (all optional / nullable):
 * <ul>
 *   <li>{@code expiryDate}      – code is invalid after this timestamp.</li>
 *   <li>{@code maxUses}         – global cap on total redemptions across all users.</li>
 *   <li>{@code maxUsesPerUser}  – per-user cap; defaults to {@code 1}.</li>
 *   <li>{@code currentUses}     – running counter incremented on each redemption.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "promo_codes")
public class PromoCode extends BaseEntity {

    /** The promo string the customer enters at checkout (e.g. {@code SUMMER20}). */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    /**
     * The percentage off the order total (0–100). Non-null only when
     * {@code discountType == PERCENTAGE}.
     */
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Column(name = "discount_value", precision = 5, scale = 2)
    private BigDecimal discountValue;

    /**
     * The menu item granted for free. Non-null only when
     * {@code discountType == FREE_ITEM}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_item_id")
    private MenuItem freeItem;

    /**
     * The user this code is locked to. {@code null} means the code is public and
     * any authenticated user may redeem it.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Optional hard expiry date-time. {@code null} = no time limit. */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    /** Maximum total redemptions across all users. {@code null} = unlimited. */
    @Column(name = "max_uses")
    private Integer maxUses;

    /** Running count of total redemptions; incremented by the service on each use. */
    @Column(name = "current_uses", nullable = false)
    private int currentUses = 0;

    /**
     * Maximum times a single user may redeem this code.
     * Defaults to {@code 1} (each user can redeem at most once).
     */
    @Column(name = "max_uses_per_user", nullable = false)
    private int maxUsesPerUser = 1;

}
