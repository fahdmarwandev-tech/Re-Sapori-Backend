package com.resapori.e_commerce.northbound.dto.offer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferSlotRequest {
    @NotBlank(message = "Slot name (EN) is required")
    private String slotNameEn;
    @NotBlank(message = "Slot name (AR) is required")
    private String slotNameAr;
    private UUID menuItemId;
    private UUID categoryId;
    private Integer quantity;
    private Integer displayOrder;
    private Boolean isFree;
    private List<UUID> eligibleItemIds;
}
