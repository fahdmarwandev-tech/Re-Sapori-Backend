package com.resapori.e_commerce.northbound.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodRevenueDto {
    private String paymentMethod;
    private String displayName;
    private BigDecimal totalRevenue;
    private Long orderCount;
    private Double percentage;
}
