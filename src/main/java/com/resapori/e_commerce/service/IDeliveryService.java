package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.delivery.CalculateDeliveryFeeRequest;
import com.resapori.e_commerce.northbound.dto.delivery.DeliveryFeeResponse;
import com.resapori.e_commerce.southbound.entity.Branch;

import java.math.BigDecimal;
import java.util.UUID;

public interface IDeliveryService {

    /**
     * Calculates the spherical geodesic distance in kilometers between two coordinate pairs
     * using the Haversine formula.
     */
    BigDecimal calculateDistanceKm(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2);

    /**
     * Calculates the delivery fee based on net distance in kilometers:
     * - If distance <= 4.0 km -> 25.00 EGP
     * - Else -> 25.00 + 7.00 * (distance - 4.0) EGP
     */
    BigDecimal calculateDeliveryFee(BigDecimal distanceKm);

    /**
     * Calculates delivery fee, distance, and nearest fulfilling branch for a given request.
     */
    DeliveryFeeResponse calculateDeliveryFee(CalculateDeliveryFeeRequest request);

    /**
     * Standalone calculation for a saved address ID.
     */
    DeliveryFeeResponse calculateForAddress(UUID addressId, UUID preferredBranchId);

    /**
     * Resolves the closest active branch with valid coordinates for delivery fulfillment.
     */
    Branch resolveBranchForDelivery(BigDecimal customerLat, BigDecimal customerLng, UUID preferredBranchId);
}
