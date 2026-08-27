package com.resapori.e_commerce.service;

import com.resapori.e_commerce.southbound.entity.Payment;
import java.util.List;
import java.util.UUID;

public interface IPaymentService {
    Payment create(Payment entity);
    Payment getById(UUID id);
    List<Payment> getAll();
    Payment update(UUID id, Payment entity);
    void delete(UUID id);
}
