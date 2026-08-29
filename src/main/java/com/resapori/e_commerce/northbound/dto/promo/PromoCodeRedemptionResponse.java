package com.resapori.e_commerce.northbound.dto.promo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromoCodeRedemptionResponse {
    private UUID id;
    private UUID promoCodeId;
    private String promoCode;
    private UUID userId;
    private UUID orderId;
    private BigDecimal discountApplied;
    private LocalDateTime redeemedAt;
}
