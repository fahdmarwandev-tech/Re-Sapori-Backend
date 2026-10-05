package com.resapori.e_commerce.northbound.dto.order;

import com.resapori.e_commerce.southbound.enums.OrderType;
import com.resapori.e_commerce.southbound.enums.PaymentMethod;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequest {
    @NotNull(message = "Order type is required")
    private OrderType orderType;
    private UUID branchId;
    private UUID addressId;
    @Valid
    private List<OrderItemInput> items;
    @Valid
    private List<OrderOfferInput> offers;
    private String promoCode;
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    @com.fasterxml.jackson.annotation.JsonAlias({"phone", "phone_number"})
    private String phoneNumber;
    @com.fasterxml.jackson.annotation.JsonAlias({"customer_phone"})
    private String customerPhone;
    @com.fasterxml.jackson.annotation.JsonAlias({"customer_name", "name"})
    private String customerName;

    public String getEffectivePhoneNumber() {
        if (phoneNumber != null && !phoneNumber.isBlank()) return phoneNumber.trim();
        if (customerPhone != null && !customerPhone.isBlank()) return customerPhone.trim();
        return null;
    }
}
