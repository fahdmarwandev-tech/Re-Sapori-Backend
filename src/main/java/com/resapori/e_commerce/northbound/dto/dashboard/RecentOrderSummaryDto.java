package com.resapori.e_commerce.northbound.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentOrderSummaryDto {
    private UUID id;
    private String orderNumber;
    private String customerName;
    private String orderType;
    private String destination;
    private String itemsSummary;
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String createdAt;
    private Long elapsedMinutes;
    private String elapsedTime;
}
