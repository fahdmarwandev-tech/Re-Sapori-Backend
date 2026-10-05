package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.dashboard.DashboardRevenueResponse;
import com.resapori.e_commerce.northbound.dto.dashboard.DashboardSummaryResponse;
import com.resapori.e_commerce.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IDashboardService dashboardService;

    /**
     * GET /api/dashboard — Consolidated dashboard summary.
     * Uses dashboard design pattern with 2 database queries max.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        return ResponseEntity.ok(dashboardService.getDashboardSummary(paymentMethod, branchId, days));
    }

    /**
     * GET /api/dashboard/revenue — Dedicated revenue analytics.
     * Supports filtering by payment method (cash, instapay, vodafone cash) in 1 single query.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue")
    public ResponseEntity<DashboardRevenueResponse> getRevenueAnalytics(
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        return ResponseEntity.ok(dashboardService.getRevenueAnalytics(paymentMethod, branchId, days));
    }
}
