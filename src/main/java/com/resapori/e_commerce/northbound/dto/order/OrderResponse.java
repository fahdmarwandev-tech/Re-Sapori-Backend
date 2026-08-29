package com.resapori.e_commerce.northbound.dto.order;

import com.resapori.e_commerce.southbound.enums.OrderStatus;
import com.resapori.e_commerce.southbound.enums.OrderType;
import com.resapori.e_commerce.southbound.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private UUID id;
    private OrderStatus status;
    private OrderType orderType;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private String currency;
    private String deliveryAddress;
    private UUID branchId;
    private String branchName;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
}
