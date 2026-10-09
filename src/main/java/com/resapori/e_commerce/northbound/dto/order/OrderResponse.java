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
    private BigDecimal deliveryFee;
    private String currency;
    private UUID addressId;
    private String deliveryAddress;
    private String street;
    private String building;
    private String floor;
    private String apartment;
    private String district;
    private String city;
    private String landmark;
    private String carPlate;
    private String carDetails;
    private BigDecimal lat;
    private BigDecimal lng;
    private String googleMapsUrl;
    private UUID branchId;
    private String branchName;
    private List<OrderItemResponse> items;
    private UUID userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String orderNotes;
    private String notes;
    private LocalDateTime createdAt;
    private String promoCode;
    private BigDecimal promoDiscountPercentage;
    private BigDecimal promoDiscountAmount;
    private BigDecimal discountAmount;
}
