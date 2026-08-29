package com.resapori.e_commerce.northbound.dto.promo;

import com.resapori.e_commerce.southbound.enums.DiscountType;
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
public class PromoCodeRequest {
    private String code;
    private String descriptionEn;
    private String descriptionAr;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private UUID freeItemId;
    private UUID userId;
    private LocalDateTime expiryDate;
    private Integer maxUses;
    @Builder.Default
    private int maxUsesPerUser = 1;
}
