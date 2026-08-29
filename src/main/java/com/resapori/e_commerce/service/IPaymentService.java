package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.payment.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface IPaymentService {
    PaymentResponse getById(UUID id);
    List<PaymentResponse> getAll();
    List<PaymentResponse> getByOrderId(UUID orderId);
}
