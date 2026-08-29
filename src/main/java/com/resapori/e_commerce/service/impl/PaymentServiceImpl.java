package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.northbound.dto.payment.PaymentResponse;
import com.resapori.e_commerce.service.IPaymentService;
import com.resapori.e_commerce.southbound.repository.IPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements IPaymentService {

    private final IPaymentRepository repository;

    @Override
    public PaymentResponse getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<PaymentResponse> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<PaymentResponse> getByOrderId(UUID orderId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
