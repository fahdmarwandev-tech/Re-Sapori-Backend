package com.resapori.e_commerce.southbound.repository.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawMenuAggregationResult {
    private List<RawTopDishDto> topDishes;
    private Long availableMenuItems;
    private Long totalMenuItems;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawTopDishDto {
        private UUID menuItemId;
        private String nameEn;
        private String nameAr;
        private String categoryName;
        private String imageUrl;
        private Long totalOrders;
        private BigDecimal totalRevenue;
    }
}
