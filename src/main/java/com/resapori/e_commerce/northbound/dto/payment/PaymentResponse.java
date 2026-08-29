package com.resapori.e_commerce.northbound.dto.payment;

import com.resapori.e_commerce.southbound.enums.PaymentMethod;
import com.resapori.e_commerce.southbound.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private String paymobOrderId;
    private String paymobTransactionId;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private Long amountCents;
    private String currency;
    private LocalDateTime createdAt;
}
