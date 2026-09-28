package com.resapori.e_commerce.northbound.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalculateDeliveryFeeRequest {

    /** Latitude of the delivery destination (from map pin or address). */
    private BigDecimal lat;

    /** Longitude of the delivery destination (from map pin or address). */
    private BigDecimal lng;

    /** Optional: ID of an existing saved address to compute delivery fee for. */
    private UUID addressId;

    /** Optional: Specific branch ID to calculate distance from. If omitted, nearest active branch is used. */
    private UUID branchId;
}
