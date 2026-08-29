package com.resapori.e_commerce.northbound.dto.menu;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemResponse {
    private UUID id;
    private UUID categoryId;
    private String categoryNameEn;
    private String nameEn;
    private String nameAr;
    private String descriptionEn;
    private String descriptionAr;
    private BigDecimal currentPrice;
    private String imageUrl;
    private boolean isAvailable;
    private Integer stock;
    private boolean isActive;
}
