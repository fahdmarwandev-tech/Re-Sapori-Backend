package com.resapori.e_commerce.northbound.dto.offer;

import com.resapori.e_commerce.southbound.enums.DiscountTarget;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferRequest {
    @NotBlank(message = "Name (EN) is required")
    private String nameEn;
    @NotBlank(message = "Name (AR) is required")
    private String nameAr;
    private String descriptionEn;
    private String descriptionAr;
    private String imageUrl;
    @NotNull(message = "Discount target is required")
    private DiscountTarget discountTarget;
    private BigDecimal discountPercentage;
    private BigDecimal fixedPrice;
    private Integer buyQuantity;
    private Integer getQuantity;
    private UUID categoryId;
    @Valid
    private List<OfferSlotRequest> slots;
}
