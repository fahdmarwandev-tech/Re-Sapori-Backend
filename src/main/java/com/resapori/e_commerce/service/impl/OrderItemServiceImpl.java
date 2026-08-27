package com.resapori.e_commerce.service.impl;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IOrderItemService;
import com.resapori.e_commerce.southbound.entity.OrderItem;
import com.resapori.e_commerce.southbound.repository.IOrderItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderItemServiceImpl implements IOrderItemService {

    private final IOrderItemRepository repository;

    @Override
    public OrderItem create(OrderItem entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public OrderItem getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<OrderItem> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public OrderItem update(UUID id, OrderItem entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
