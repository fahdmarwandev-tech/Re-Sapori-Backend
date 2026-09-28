package com.resapori.e_commerce.northbound.dto.offer;

import com.resapori.e_commerce.southbound.enums.DiscountTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferResponse {
    private UUID id;
    private String nameEn;
    private String nameAr;
    private String descriptionEn;
    private String descriptionAr;
    private String imageUrl;
    private DiscountTarget discountTarget;
    private BigDecimal discountPercentage;
    private BigDecimal fixedPrice;
    private Integer buyQuantity;
    private Integer getQuantity;
    private UUID categoryId;
    private String categoryNameEn;
    private String categoryNameAr;
    private List<OfferSlotResponse> slots;
    private boolean active;
    private LocalDateTime createdAt;
}
