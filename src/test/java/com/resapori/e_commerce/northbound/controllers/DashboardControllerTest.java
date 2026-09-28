package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.dashboard.DashboardRevenueResponse;
import com.resapori.e_commerce.northbound.dto.dashboard.DashboardSummaryResponse;
import com.resapori.e_commerce.northbound.dto.dashboard.KpiMetricsDto;
import com.resapori.e_commerce.service.IDashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardControllerTest {

    private final IDashboardService dashboardService = Mockito.mock(IDashboardService.class);
    private final DashboardController controller = new DashboardController(dashboardService);

    @Test
    @DisplayName("getDashboardSummary should delegate to service and return 200 OK")
    void shouldReturnOkForDashboardSummary() {
        DashboardSummaryResponse mockSummary = DashboardSummaryResponse.builder()
                .filteredPaymentMethod("CASH")
                .kpis(KpiMetricsDto.builder().todayRevenue(BigDecimal.valueOf(184520)).build())
                .build();

        UUID branchId = UUID.randomUUID();
        when(dashboardService.getDashboardSummary("CASH", branchId, 7)).thenReturn(mockSummary);

        ResponseEntity<DashboardSummaryResponse> response = controller.getDashboardSummary("CASH", branchId, 7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CASH", response.getBody().getFilteredPaymentMethod());
        assertEquals(BigDecimal.valueOf(184520), response.getBody().getKpis().getTodayRevenue());
        verify(dashboardService).getDashboardSummary("CASH", branchId, 7);
    }

    @Test
    @DisplayName("getRevenueAnalytics should delegate to service and return 200 OK")
    void shouldReturnOkForRevenueAnalytics() {
        DashboardRevenueResponse mockRevenue = DashboardRevenueResponse.builder()
                .filteredPaymentMethod("INSTAPAY")
                .todayRevenue(BigDecimal.valueOf(60000))
                .build();

        when(dashboardService.getRevenueAnalytics("INSTAPAY", null, 7)).thenReturn(mockRevenue);

        ResponseEntity<DashboardRevenueResponse> response = controller.getRevenueAnalytics("INSTAPAY", null, 7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INSTAPAY", response.getBody().getFilteredPaymentMethod());
        assertEquals(BigDecimal.valueOf(60000), response.getBody().getTodayRevenue());
        verify(dashboardService).getRevenueAnalytics("INSTAPAY", null, 7);
    }
}
