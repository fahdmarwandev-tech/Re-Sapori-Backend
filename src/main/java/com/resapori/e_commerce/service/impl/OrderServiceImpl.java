package com.resapori.e_commerce.service.impl;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IOrderService;
import com.resapori.e_commerce.southbound.entity.Order;
import com.resapori.e_commerce.southbound.repository.IOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository repository;

    @Override
    public Order create(Order entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Order getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Order> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Order update(UUID id, Order entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
