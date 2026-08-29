package com.resapori.e_commerce.northbound.dto.order;

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
public class OrderItemResponse {
    private UUID id;
    private UUID menuItemId;
    private String nameEn;
    private String nameAr;
    private int quantity;
    private BigDecimal unitPriceAtPurchase;
    private BigDecimal lineTotal;
}
