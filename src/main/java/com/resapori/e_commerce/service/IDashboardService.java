package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.dashboard.DashboardRevenueResponse;
import com.resapori.e_commerce.northbound.dto.dashboard.DashboardSummaryResponse;

import java.util.UUID;

public interface IDashboardService {

    DashboardSummaryResponse getDashboardSummary(String paymentMethod, UUID branchId, Integer days);

    DashboardRevenueResponse getRevenueAnalytics(String paymentMethod, UUID branchId, Integer days);
}
