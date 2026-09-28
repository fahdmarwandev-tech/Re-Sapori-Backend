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
public class KpiMetricsDto {
    private BigDecimal todayRevenue;
    private BigDecimal yesterdayRevenue;
    private Double revenueComparisonPercentage;
    private Long totalOrdersToday;
    private Long totalOrdersYesterday;
    private Double ordersComparisonPercentage;
    private Long pendingOrders;
    private Long preparingOrders;
    private Long readyOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    private Long activeMenuItems;
    private Long totalMenuItems;
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Double fulfillmentRate;
}
