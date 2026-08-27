package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link PromoCode} entities.
 *
 * <p>Provides the core queries needed for code validation:
 * <ul>
 *   <li>Look up a code by its string value (the most common read path).</li>
 *   <li>Atomically increment {@code current_uses} inside a transaction when a code is redeemed.</li>
 * </ul>
 */
@Repository
public interface IPromoCodeRepository extends JpaRepository<PromoCode, UUID> {

    /**
     * Finds an active promo code by its code string.
     * The service uses this as the entry point for all validation logic.
     *
     * @param code the promo code string (case-sensitive, e.g. {@code "SUMMER20"})
     * @return the matching active promo code, or empty if not found / inactive
     */
    Optional<PromoCode> findByCodeAndIsActiveTrue(String code);

    /**
     * Atomically increments {@code current_uses} by 1 for the given promo code.
     * Must be called inside a transaction after all eligibility checks have passed.
     *
     * @param id the promo code UUID
     */
    @Modifying
    @Query("UPDATE PromoCode p SET p.currentUses = p.currentUses + 1 WHERE p.id = :id")
    void incrementCurrentUses(@Param("id") UUID id);
}
