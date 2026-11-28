package com.resapori.e_commerce.northbound.dto.promo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WelcomeEligibilityResponse {
    private boolean eligible;
    private String code;
    private BigDecimal discountPercentage;
}
