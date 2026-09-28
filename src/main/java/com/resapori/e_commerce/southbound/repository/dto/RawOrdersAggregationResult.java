package com.resapori.e_commerce.southbound.repository.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawOrdersAggregationResult {
    private BigDecimal todayRevenue;
    private BigDecimal yesterdayRevenue;
    private Long totalOrdersToday;
    private Long totalOrdersYesterday;
    private Long pendingOrdersCount;
    private Long preparingOrdersCount;
    private Long readyOrdersCount;
    private Long deliveredOrdersCount;
    private Long cancelledOrdersCount;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private List<RawPaymentMethodDto> revenueByPaymentMethod;
    private List<RawDailyTrendDto> dailyTrends;
    private List<RawRecentOrderDto> recentOrders;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawPaymentMethodDto {
        private String payment_method;
        private BigDecimal total_revenue;
        private Long order_count;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawDailyTrendDto {
        private String date_str;
        private String day_name;
        private BigDecimal revenue;
        private Long order_count;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawRecentOrderDto {
        private String id;
        private String order_number;
        private String customer_name;
        private String order_type;
        private String destination;
        private String items_summary;
        private BigDecimal total_amount;
        private String currency;
        private String status;
        private String payment_method;
        private String created_at;
        private Long elapsed_minutes;
    }
}
