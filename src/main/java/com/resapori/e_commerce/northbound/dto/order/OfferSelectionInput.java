package com.resapori.e_commerce.northbound.dto.order;

import com.resapori.e_commerce.southbound.enums.ItemSize;
import jakarta.validation.constraints.NotNull;
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
public class OfferSelectionInput {
    @NotNull(message = "Slot ID is required")
    private UUID slotId;
    @NotNull(message = "Menu item ID is required")
    private UUID menuItemId;
    @Builder.Default
    private Integer quantity = 1;
    @Builder.Default
    private ItemSize size = ItemSize.REGULAR;
    private List<UUID> addOnIds;
    private String notes;
}
