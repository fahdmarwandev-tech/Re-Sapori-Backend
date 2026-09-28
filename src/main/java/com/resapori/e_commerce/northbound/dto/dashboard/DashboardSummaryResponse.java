package com.resapori.e_commerce.northbound.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryResponse {
    private String filteredPaymentMethod;
    private KpiMetricsDto kpis;
    private List<PaymentMethodRevenueDto> revenueByPaymentMethod;
    private List<RevenueTrendDto> revenueTrends;
    private List<OrderStatusBreakdownDto> ordersByStatus;
    private List<RecentOrderSummaryDto> recentOrders;
    private List<TopDishDto> topDishes;
}
