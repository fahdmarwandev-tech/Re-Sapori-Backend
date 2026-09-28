package com.resapori.e_commerce.northbound.dto.offer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.resapori.e_commerce.northbound.dto.menu.MenuItemResponse;
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
public class OfferSlotResponse {
    private UUID id;
    private String slotNameEn;
    private String slotNameAr;

    @JsonProperty("isFree")
    private boolean isFree;

    private Integer quantity;
    private Integer displayOrder;
    private MenuItemResponse fixedItem;
    private UUID eligibleCategoryId;
    private String eligibleCategoryNameEn;
    private String eligibleCategoryNameAr;
    private List<MenuItemResponse> eligibleItems;

    @JsonProperty("isFree")
    public boolean isFree() {
        return isFree;
    }

    @JsonProperty("isFree")
    public void setFree(boolean isFree) {
        this.isFree = isFree;
    }
}
