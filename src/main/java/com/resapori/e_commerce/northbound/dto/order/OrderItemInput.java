package com.resapori.e_commerce.northbound.dto.order;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemInput {
    private UUID menuItemId;
    private int quantity;
}
