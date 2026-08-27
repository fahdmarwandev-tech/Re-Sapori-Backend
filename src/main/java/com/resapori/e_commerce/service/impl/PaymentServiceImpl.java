package com.resapori.e_commerce.service.impl;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IPaymentService;
import com.resapori.e_commerce.southbound.entity.Payment;
import com.resapori.e_commerce.southbound.repository.IPaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements IPaymentService {

    private final IPaymentRepository repository;

    @Override
    public Payment create(Payment entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Payment getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Payment> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Payment update(UUID id, Payment entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
