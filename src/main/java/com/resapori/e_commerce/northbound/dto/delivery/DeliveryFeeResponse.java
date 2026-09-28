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
public class DeliveryFeeResponse {

    /** Geodesic distance between customer location and fulfilling branch in kilometers. */
    private BigDecimal distanceKm;

    /** Calculated delivery shipping fee in EGP. */
    private BigDecimal deliveryFee;

    /** ID of the branch fulfilling this delivery (nearest active branch). */
    private UUID branchId;

    /** Name of the branch fulfilling this delivery. */
    private String branchName;

    /** Currency code, typically EGP. */
    @Builder.Default
    private String currency = "EGP";
}
