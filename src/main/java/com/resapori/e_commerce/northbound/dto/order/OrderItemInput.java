package com.resapori.e_commerce.northbound.dto.order;

import com.resapori.e_commerce.southbound.enums.ItemSize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemInput {
    @NotNull(message = "Menu item ID is required")
    private UUID menuItemId;
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
    /**
     * Defaults to REGULAR. Set to MINI only for items that support a mini portion
     * (i.e. MenuItem.miniPrice is non-null). A validation error is thrown at order
     * time if MINI is requested for an item without a miniPrice.
     */
    @Builder.Default
    private ItemSize size = ItemSize.REGULAR;
}
