package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.repository.dto.RawMenuAggregationResult;
import com.resapori.e_commerce.southbound.repository.dto.RawOrdersAggregationResult;

import java.util.UUID;

public interface IDashboardRepository {

    RawOrdersAggregationResult fetchOrdersAndRevenueAggregate(String paymentMethod, UUID branchId, int days);

    RawMenuAggregationResult fetchMenuAnalytics(UUID branchId);
}
