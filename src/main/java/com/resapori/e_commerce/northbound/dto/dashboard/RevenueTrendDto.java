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
public class RevenueTrendDto {
    private String date;
    private String dayOfWeek;
    private BigDecimal revenue;
    private Long orderCount;
    private Boolean isPeak;
}
