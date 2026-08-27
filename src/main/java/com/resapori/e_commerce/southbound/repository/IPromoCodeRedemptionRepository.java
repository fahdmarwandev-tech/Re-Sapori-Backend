package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.PromoCodeRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link PromoCodeRedemption} entities.
 *
 * <p>Key queries:
 * <ul>
 *   <li>Count how many times a specific user has redeemed a specific code
 *       (used to enforce {@code maxUsesPerUser}).</li>
 *   <li>Fetch all redemptions for a promo code (admin analytics).</li>
 *   <li>Fetch all redemptions by a user (customer history page).</li>
 *   <li>Check whether a specific order already has a redemption applied
 *       (duplicate-apply guard).</li>
 * </ul>
 */
@Repository
public interface IPromoCodeRedemptionRepository extends JpaRepository<PromoCodeRedemption, UUID> {

    /**
     * Counts how many times the given user has already redeemed the given promo code.
     * Used to enforce {@link com.resapori.e_commerce.southbound.entity.PromoCode#getMaxUsesPerUser()}.
     *
     * @param promoCodeId the promo code UUID
     * @param userId      the user UUID
     * @return number of past redemptions
     */
    long countByPromoCodeIdAndUserId(UUID promoCodeId, UUID userId);

    /**
     * Returns all redemption records for a given promo code.
     * Intended for admin dashboards and analytics.
     *
     * @param promoCodeId the promo code UUID
     */
    List<PromoCodeRedemption> findByPromoCodeId(UUID promoCodeId);

    /**
     * Returns all redemption records for a given user.
     * Intended for the customer's order history / promotions page.
     *
     * @param userId the user UUID
     */
    List<PromoCodeRedemption> findByUserId(UUID userId);

    /**
     * Checks whether a promo code has already been applied to a specific order.
     * Prevents double-application within the same request under race conditions.
     *
     * @param orderId the order UUID
     * @return the existing redemption, or empty if none exists
     */
    Optional<PromoCodeRedemption> findByOrderId(UUID orderId);
}
