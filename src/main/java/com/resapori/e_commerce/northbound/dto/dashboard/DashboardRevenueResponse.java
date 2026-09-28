package com.resapori.e_commerce.northbound.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardRevenueResponse {
    private String filteredPaymentMethod;
    private BigDecimal todayRevenue;
    private BigDecimal yesterdayRevenue;
    private Double revenueComparisonPercentage;
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private List<RevenueTrendDto> dailyTrends;
    private List<PaymentMethodRevenueDto> breakdownByPaymentMethod;
}
