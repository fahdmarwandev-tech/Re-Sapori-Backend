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
public class TopDishDto {
    private UUID menuItemId;
    private String nameEn;
    private String nameAr;
    private String categoryName;
    private String imageUrl;
    private Long totalOrders;
    private BigDecimal totalRevenue;
}
