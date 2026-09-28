package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.dashboard.DashboardRevenueResponse;
import com.resapori.e_commerce.northbound.dto.dashboard.DashboardSummaryResponse;
import com.resapori.e_commerce.service.impl.DashboardServiceImpl;
import com.resapori.e_commerce.southbound.repository.IDashboardRepository;
import com.resapori.e_commerce.southbound.repository.dto.RawMenuAggregationResult;
import com.resapori.e_commerce.southbound.repository.dto.RawOrdersAggregationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private IDashboardRepository dashboardRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private RawOrdersAggregationResult sampleOrdersData;
    private RawMenuAggregationResult sampleMenuData;

    @BeforeEach
    void setUp() {
        sampleOrdersData = new RawOrdersAggregationResult();
        sampleOrdersData.setTodayRevenue(BigDecimal.valueOf(184520));
        sampleOrdersData.setYesterdayRevenue(BigDecimal.valueOf(161500));
        sampleOrdersData.setTotalOrdersToday(342L);
        sampleOrdersData.setTotalOrdersYesterday(316L);
        sampleOrdersData.setPendingOrdersCount(12L);
        sampleOrdersData.setPreparingOrdersCount(18L);
        sampleOrdersData.setReadyOrdersCount(9L);
        sampleOrdersData.setDeliveredOrdersCount(294L);
        sampleOrdersData.setCancelledOrdersCount(9L);
        sampleOrdersData.setTotalOrders(342L);
        sampleOrdersData.setTotalRevenue(BigDecimal.valueOf(184520));

        RawOrdersAggregationResult.RawPaymentMethodDto pmCash = new RawOrdersAggregationResult.RawPaymentMethodDto();
        pmCash.setPayment_method("CASH");
        pmCash.setTotal_revenue(BigDecimal.valueOf(90000));
        pmCash.setOrder_count(180L);

        RawOrdersAggregationResult.RawPaymentMethodDto pmInstapay = new RawOrdersAggregationResult.RawPaymentMethodDto();
        pmInstapay.setPayment_method("INSTAPAY");
        pmInstapay.setTotal_revenue(BigDecimal.valueOf(60000));
        pmInstapay.setOrder_count(100L);

        RawOrdersAggregationResult.RawPaymentMethodDto pmVodafone = new RawOrdersAggregationResult.RawPaymentMethodDto();
        pmVodafone.setPayment_method("VODAFONE_CASH");
        pmVodafone.setTotal_revenue(BigDecimal.valueOf(34520));
        pmVodafone.setOrder_count(62L);

        sampleOrdersData.setRevenueByPaymentMethod(List.of(pmCash, pmInstapay, pmVodafone));

        RawOrdersAggregationResult.RawDailyTrendDto trendThu = new RawOrdersAggregationResult.RawDailyTrendDto();
        trendThu.setDate_str("2026-09-24");
        trendThu.setDay_name("Thu");
        trendThu.setRevenue(BigDecimal.valueOf(215400));
        trendThu.setOrder_count(380L);

        RawOrdersAggregationResult.RawDailyTrendDto trendFri = new RawOrdersAggregationResult.RawDailyTrendDto();
        trendFri.setDate_str("2026-09-25");
        trendFri.setDay_name("Fri");
        trendFri.setRevenue(BigDecimal.valueOf(175000));
        trendFri.setOrder_count(310L);

        sampleOrdersData.setDailyTrends(List.of(trendThu, trendFri));

        RawOrdersAggregationResult.RawRecentOrderDto ro1 = new RawOrdersAggregationResult.RawRecentOrderDto();
        ro1.setId(UUID.randomUUID().toString());
        ro1.setOrder_number("#RS-4821");
        ro1.setCustomer_name("Amira Mansour");
        ro1.setOrder_type("DINE_IN");
        ro1.setDestination("Table 12 (VIP)");
        ro1.setItems_summary("Truffle Tagliolini x2");
        ro1.setTotal_amount(BigDecimal.valueOf(4250));
        ro1.setCurrency("EGP");
        ro1.setStatus("PREPARING");
        ro1.setPayment_method("INSTAPAY");
        ro1.setCreated_at("2026-09-28T10:00:00Z");
        ro1.setElapsed_minutes(4L);

        sampleOrdersData.setRecentOrders(List.of(ro1));

        sampleMenuData = new RawMenuAggregationResult();
        sampleMenuData.setAvailableMenuItems(86L);
        sampleMenuData.setTotalMenuItems(92L);

        RawMenuAggregationResult.RawTopDishDto topDish = new RawMenuAggregationResult.RawTopDishDto();
        topDish.setMenuItemId(UUID.randomUUID());
        topDish.setNameEn("Tagliolini al Tartufo");
        topDish.setNameAr("تاغليوليني بالت truffle");
        topDish.setCategoryName("Pasta");
        topDish.setImageUrl("https://example.com/pasta.jpg");
        topDish.setTotalOrders(48L);
        topDish.setTotalRevenue(BigDecimal.valueOf(43200));

        sampleMenuData.setTopDishes(List.of(topDish));
    }

    @Test
    @DisplayName("getDashboardSummary should return populated summary with exactly 2 repository calls")
    void testGetDashboardSummary() {
        when(dashboardRepository.fetchOrdersAndRevenueAggregate(eq("CASH"), any(), eq(7)))
                .thenReturn(sampleOrdersData);
        when(dashboardRepository.fetchMenuAnalytics(any()))
                .thenReturn(sampleMenuData);

        DashboardSummaryResponse res = dashboardService.getDashboardSummary("CASH", null, 7);

        assertNotNull(res);
        assertEquals("CASH", res.getFilteredPaymentMethod());
        assertEquals(BigDecimal.valueOf(184520), res.getKpis().getTodayRevenue());
        assertTrue(res.getKpis().getRevenueComparisonPercentage() > 0);
        assertEquals(342L, res.getKpis().getTotalOrdersToday());
        assertEquals(12L, res.getKpis().getPendingOrders());
        assertEquals(86L, res.getKpis().getActiveMenuItems());
        assertEquals(92L, res.getKpis().getTotalMenuItems());

        assertEquals(3, res.getRevenueByPaymentMethod().size());
        assertEquals("Cash", res.getRevenueByPaymentMethod().get(0).getDisplayName());
        assertEquals("InstaPay", res.getRevenueByPaymentMethod().get(1).getDisplayName());
        assertEquals("Vodafone Cash", res.getRevenueByPaymentMethod().get(2).getDisplayName());

        assertEquals(2, res.getRevenueTrends().size());
        assertTrue(res.getRevenueTrends().get(0).getIsPeak());

        assertEquals(5, res.getOrdersByStatus().size());
        assertEquals(1, res.getRecentOrders().size());
        assertEquals("4m ago", res.getRecentOrders().get(0).getElapsedTime());
        assertEquals("Amira Mansour", res.getRecentOrders().get(0).getCustomerName());

        assertEquals(1, res.getTopDishes().size());
        assertEquals("Tagliolini al Tartufo", res.getTopDishes().get(0).getNameEn());

        verify(dashboardRepository).fetchOrdersAndRevenueAggregate(eq("CASH"), any(), eq(7));
        verify(dashboardRepository).fetchMenuAnalytics(any());
    }

    @Test
    @DisplayName("getRevenueAnalytics should filter by payment method with exactly 1 repository call")
    void testGetRevenueAnalytics() {
        when(dashboardRepository.fetchOrdersAndRevenueAggregate(eq("INSTAPAY"), any(), eq(7)))
                .thenReturn(sampleOrdersData);

        DashboardRevenueResponse res = dashboardService.getRevenueAnalytics("INSTAPAY", null, 7);

        assertNotNull(res);
        assertEquals("INSTAPAY", res.getFilteredPaymentMethod());
        assertEquals(BigDecimal.valueOf(184520), res.getTodayRevenue());
        assertEquals(342L, res.getTotalOrders());
        assertEquals(2, res.getDailyTrends().size());
        assertEquals(3, res.getBreakdownByPaymentMethod().size());

        verify(dashboardRepository).fetchOrdersAndRevenueAggregate(eq("INSTAPAY"), any(), eq(7));
    }

    @Test
    @DisplayName("getDashboardSummary should gracefully handle empty data without NPE")
    void testGetDashboardSummary_Empty() {
        RawOrdersAggregationResult emptyOrders = new RawOrdersAggregationResult();
        RawMenuAggregationResult emptyMenu = new RawMenuAggregationResult();

        when(dashboardRepository.fetchOrdersAndRevenueAggregate(any(), any(), anyInt()))
                .thenReturn(emptyOrders);
        when(dashboardRepository.fetchMenuAnalytics(any()))
                .thenReturn(emptyMenu);

        DashboardSummaryResponse res = dashboardService.getDashboardSummary(null, null, null);

        assertNotNull(res);
        assertEquals("ALL", res.getFilteredPaymentMethod());
        assertEquals(BigDecimal.ZERO, res.getKpis().getTodayRevenue());
        assertEquals(0L, res.getKpis().getTotalOrdersToday());
        assertEquals(0.0, res.getKpis().getRevenueComparisonPercentage());
        assertEquals(100.0, res.getKpis().getFulfillmentRate());
    }
}
