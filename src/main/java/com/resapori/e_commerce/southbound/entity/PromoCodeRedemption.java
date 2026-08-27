package com.resapori.e_commerce.southbound.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit record written every time a {@link PromoCode} is successfully applied to an {@link Order}.
 *
 * <p>This table serves two purposes:
 * <ol>
 *   <li><b>Eligibility checking</b> – the service counts rows per {@code (promoCode, user)}
 *       to enforce {@link PromoCode#getMaxUsesPerUser()}.</li>
 *   <li><b>Auditing</b> – {@code discountApplied} is a monetary snapshot of the actual discount
 *       at the time of redemption, independent of any future changes to the promo code.</li>
 * </ol>
 *
 * <p>This entity does NOT extend {@link BaseEntity} because it is a lightweight join/audit record
 * that does not need the full audit trail of {@code created_by}, {@code updated_by}, or
 * {@code is_active}. A simple {@code redeemed_at} timestamp is sufficient.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "promo_code_redemptions",
    uniqueConstraints = {
        // A promo code can be applied to any given order at most once
        @UniqueConstraint(name = "uq_promo_redemptions_order", columnNames = "order_id")
    }
)
public class PromoCodeRedemption {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id", nullable = false)
    private PromoCode promoCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Monetary snapshot of the discount that was actually applied to the order.
     * For a FREE_ITEM code this holds the market price of the free item at redemption time.
     */
    @Column(name = "discount_applied", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountApplied;

    /** The exact moment the promo code was validated and applied. */
    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private LocalDateTime redeemedAt = LocalDateTime.now();

}
