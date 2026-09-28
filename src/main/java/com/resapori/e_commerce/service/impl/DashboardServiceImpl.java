package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.northbound.dto.dashboard.*;
import com.resapori.e_commerce.service.IDashboardService;
import com.resapori.e_commerce.southbound.repository.IDashboardRepository;
import com.resapori.e_commerce.southbound.repository.dto.RawMenuAggregationResult;
import com.resapori.e_commerce.southbound.repository.dto.RawOrdersAggregationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RequiredArgsConstructor
@Service
public class DashboardServiceImpl implements IDashboardService {

    private final IDashboardRepository dashboardRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(String paymentMethod, UUID branchId, Integer days) {
        int rangeDays = resolveDays(days);
        RawOrdersAggregationResult ordersData = dashboardRepository.fetchOrdersAndRevenueAggregate(paymentMethod, branchId, rangeDays);
        RawMenuAggregationResult menuData = dashboardRepository.fetchMenuAnalytics(branchId);

        return DashboardSummaryResponse.builder()
                .filteredPaymentMethod(paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod.toUpperCase() : "ALL")
                .kpis(buildKpiMetrics(ordersData, menuData))
                .revenueByPaymentMethod(mapPaymentMethods(ordersData))
                .revenueTrends(mapRevenueTrends(ordersData))
                .ordersByStatus(mapStatusBreakdown(ordersData))
                .recentOrders(mapRecentOrders(ordersData))
                .topDishes(mapTopDishes(menuData))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardRevenueResponse getRevenueAnalytics(String paymentMethod, UUID branchId, Integer days) {
        int rangeDays = resolveDays(days);
        RawOrdersAggregationResult ordersData = dashboardRepository.fetchOrdersAndRevenueAggregate(paymentMethod, branchId, rangeDays);
        BigDecimal todayRev = ordersData.getTodayRevenue() != null ? ordersData.getTodayRevenue() : BigDecimal.ZERO;
        BigDecimal yestRev = ordersData.getYesterdayRevenue() != null ? ordersData.getYesterdayRevenue() : BigDecimal.ZERO;

        return DashboardRevenueResponse.builder()
                .filteredPaymentMethod(paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod.toUpperCase() : "ALL")
                .todayRevenue(todayRev)
                .yesterdayRevenue(yestRev)
                .revenueComparisonPercentage(calculateComparisonPercentage(todayRev, yestRev))
                .totalRevenue(ordersData.getTotalRevenue() != null ? ordersData.getTotalRevenue() : BigDecimal.ZERO)
                .totalOrders(ordersData.getTotalOrders() != null ? ordersData.getTotalOrders() : 0L)
                .dailyTrends(mapRevenueTrends(ordersData))
                .breakdownByPaymentMethod(mapPaymentMethods(ordersData))
                .build();
    }

    private int resolveDays(Integer days) {
        return (days != null && days > 0) ? days : 7;
    }

    private KpiMetricsDto buildKpiMetrics(RawOrdersAggregationResult o, RawMenuAggregationResult m) {
        BigDecimal todayRev = o.getTodayRevenue() != null ? o.getTodayRevenue() : BigDecimal.ZERO;
        BigDecimal yestRev = o.getYesterdayRevenue() != null ? o.getYesterdayRevenue() : BigDecimal.ZERO;
        Long todayOrders = o.getTotalOrdersToday() != null ? o.getTotalOrdersToday() : 0L;
        Long yestOrders = o.getTotalOrdersYesterday() != null ? o.getTotalOrdersYesterday() : 0L;
        Long delivered = o.getDeliveredOrdersCount() != null ? o.getDeliveredOrdersCount() : 0L;
        Long total = o.getTotalOrders() != null ? o.getTotalOrders() : 0L;

        return KpiMetricsDto.builder()
                .todayRevenue(todayRev)
                .yesterdayRevenue(yestRev)
                .revenueComparisonPercentage(calculateComparisonPercentage(todayRev, yestRev))
                .totalOrdersToday(todayOrders)
                .totalOrdersYesterday(yestOrders)
                .ordersComparisonPercentage(calculateOrdersComparisonPercentage(todayOrders, yestOrders))
                .pendingOrders(o.getPendingOrdersCount() != null ? o.getPendingOrdersCount() : 0L)
                .preparingOrders(o.getPreparingOrdersCount() != null ? o.getPreparingOrdersCount() : 0L)
                .readyOrders(o.getReadyOrdersCount() != null ? o.getReadyOrdersCount() : 0L)
                .deliveredOrders(delivered)
                .cancelledOrders(o.getCancelledOrdersCount() != null ? o.getCancelledOrdersCount() : 0L)
                .activeMenuItems(m.getAvailableMenuItems() != null ? m.getAvailableMenuItems() : 0L)
                .totalMenuItems(m.getTotalMenuItems() != null ? m.getTotalMenuItems() : 0L)
                .totalRevenue(o.getTotalRevenue() != null ? o.getTotalRevenue() : BigDecimal.ZERO)
                .totalOrders(total)
                .fulfillmentRate(calculateFulfillmentRate(delivered, total))
                .build();
    }

    private Double calculateComparisonPercentage(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        BigDecimal diff = current.subtract(previous);
        BigDecimal pct = diff.divide(previous, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        return pct.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private Double calculateOrdersComparisonPercentage(Long current, Long previous) {
        if (previous == 0L) {
            return current > 0L ? 100.0 : 0.0;
        }
        double diff = (double) (current - previous);
        double pct = (diff / (double) previous) * 100.0;
        return BigDecimal.valueOf(pct).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private Double calculateFulfillmentRate(Long delivered, Long total) {
        if (total == 0L) return 100.0;
        double rate = ((double) delivered / (double) total) * 100.0;
        return BigDecimal.valueOf(rate).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private List<PaymentMethodRevenueDto> mapPaymentMethods(RawOrdersAggregationResult ordersData) {
        if (ordersData.getRevenueByPaymentMethod() == null) return Collections.emptyList();
        BigDecimal totalRev = ordersData.getTotalRevenue() != null && ordersData.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0
                ? ordersData.getTotalRevenue() : BigDecimal.ONE;

        return ordersData.getRevenueByPaymentMethod().stream()
                .map(raw -> toPaymentMethodDto(raw, totalRev))
                .toList();
    }

    private PaymentMethodRevenueDto toPaymentMethodDto(RawOrdersAggregationResult.RawPaymentMethodDto raw, BigDecimal totalRev) {
        BigDecimal rev = raw.getTotal_revenue() != null ? raw.getTotal_revenue() : BigDecimal.ZERO;
        double pct = rev.divide(totalRev, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        return PaymentMethodRevenueDto.builder()
                .paymentMethod(raw.getPayment_method())
                .displayName(resolveDisplayName(raw.getPayment_method()))
                .totalRevenue(rev)
                .orderCount(raw.getOrder_count() != null ? raw.getOrder_count() : 0L)
                .percentage(pct)
                .build();
    }

    private String resolveDisplayName(String method) {
        if (method == null) return "Unknown";
        return switch (method.toUpperCase()) {
            case "CASH", "CASH_ON_DELIVERY" -> "Cash";
            case "INSTAPAY" -> "InstaPay";
            case "VODAFONE_CASH", "WALLET" -> "Vodafone Cash";
            case "CARD" -> "Credit Card";
            case "KIOSK" -> "Aman / Kiosk";
            default -> method;
        };
    }

    private List<RevenueTrendDto> mapRevenueTrends(RawOrdersAggregationResult ordersData) {
        if (ordersData.getDailyTrends() == null) return Collections.emptyList();
        BigDecimal peakRevenue = findPeakRevenue(ordersData.getDailyTrends());

        return ordersData.getDailyTrends().stream()
                .map(raw -> toTrendDto(raw, peakRevenue))
                .toList();
    }

    private RevenueTrendDto toTrendDto(RawOrdersAggregationResult.RawDailyTrendDto raw, BigDecimal peak) {
        BigDecimal rev = raw.getRevenue() != null ? raw.getRevenue() : BigDecimal.ZERO;
        boolean isPeak = peak.compareTo(BigDecimal.ZERO) > 0 && rev.compareTo(peak) == 0;
        return RevenueTrendDto.builder()
                .date(raw.getDate_str())
                .dayOfWeek(raw.getDay_name())
                .revenue(rev)
                .orderCount(raw.getOrder_count() != null ? raw.getOrder_count() : 0L)
                .isPeak(isPeak)
                .build();
    }

    private BigDecimal findPeakRevenue(List<RawOrdersAggregationResult.RawDailyTrendDto> trends) {
        return trends.stream()
                .map(t -> t.getRevenue() != null ? t.getRevenue() : BigDecimal.ZERO)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private List<OrderStatusBreakdownDto> mapStatusBreakdown(RawOrdersAggregationResult o) {
        long total = o.getTotalOrders() != null && o.getTotalOrders() > 0 ? o.getTotalOrders() : 1L;
        List<OrderStatusBreakdownDto> list = new ArrayList<>();
        list.add(buildStatusDto("PENDING", o.getPendingOrdersCount(), total, "#DF9926"));
        list.add(buildStatusDto("PREPARING", o.getPreparingOrdersCount(), total, "#FFB952"));
        list.add(buildStatusDto("READY", o.getReadyOrdersCount(), total, "#70D6FF"));
        list.add(buildStatusDto("DELIVERED", o.getDeliveredOrdersCount(), total, "#138A3B"));
        list.add(buildStatusDto("CANCELLED", o.getCancelledOrdersCount(), total, "#C62828"));
        return list;
    }

    private OrderStatusBreakdownDto buildStatusDto(String status, Long count, long total, String color) {
        long safeCount = count != null ? count : 0L;
        double pct = BigDecimal.valueOf((safeCount * 100.0) / total).setScale(1, RoundingMode.HALF_UP).doubleValue();
        return OrderStatusBreakdownDto.builder()
                .status(status)
                .count(safeCount)
                .percentage(pct)
                .badgeColor(color)
                .build();
    }

    private List<RecentOrderSummaryDto> mapRecentOrders(RawOrdersAggregationResult ordersData) {
        if (ordersData.getRecentOrders() == null) return Collections.emptyList();
        return ordersData.getRecentOrders().stream()
                .map(this::toRecentOrderDto)
                .toList();
    }

    private RecentOrderSummaryDto toRecentOrderDto(RawOrdersAggregationResult.RawRecentOrderDto raw) {
        return RecentOrderSummaryDto.builder()
                .id(raw.getId() != null ? UUID.fromString(raw.getId()) : null)
                .orderNumber(raw.getOrder_number())
                .customerName(raw.getCustomer_name() != null && !raw.getCustomer_name().isBlank() ? raw.getCustomer_name() : "Guest")
                .orderType(raw.getOrder_type())
                .destination(raw.getDestination())
                .itemsSummary(raw.getItems_summary())
                .totalAmount(raw.getTotal_amount() != null ? raw.getTotal_amount() : BigDecimal.ZERO)
                .currency(raw.getCurrency() != null ? raw.getCurrency() : "EGP")
                .status(raw.getStatus())
                .paymentMethod(raw.getPayment_method())
                .createdAt(raw.getCreated_at())
                .elapsedMinutes(raw.getElapsed_minutes() != null ? raw.getElapsed_minutes() : 0L)
                .elapsedTime(formatElapsedTime(raw.getElapsed_minutes()))
                .build();
    }

    private String formatElapsedTime(Long minutes) {
        if (minutes == null || minutes <= 0L) return "Just now";
        if (minutes < 60L) return minutes + "m ago";
        if (minutes < 1440L) return (minutes / 60L) + "h ago";
        return (minutes / 1440L) + "d ago";
    }

    private List<TopDishDto> mapTopDishes(RawMenuAggregationResult menuData) {
        if (menuData.getTopDishes() == null) return Collections.emptyList();
        return menuData.getTopDishes().stream()
                .map(this::toTopDishDto)
                .toList();
    }

    private TopDishDto toTopDishDto(RawMenuAggregationResult.RawTopDishDto raw) {
        return TopDishDto.builder()
                .menuItemId(raw.getMenuItemId())
                .nameEn(raw.getNameEn())
                .nameAr(raw.getNameAr())
                .categoryName(raw.getCategoryName())
                .imageUrl(raw.getImageUrl())
                .totalOrders(raw.getTotalOrders() != null ? raw.getTotalOrders() : 0L)
                .totalRevenue(raw.getTotalRevenue() != null ? raw.getTotalRevenue() : BigDecimal.ZERO)
                .build();
    }
}
