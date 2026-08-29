package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.northbound.dto.order.OrderItemRequest;
import com.resapori.e_commerce.northbound.dto.order.OrderItemResponse;
import com.resapori.e_commerce.service.IOrderItemService;
import com.resapori.e_commerce.southbound.repository.IOrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderItemServiceImpl implements IOrderItemService {

    private final IOrderItemRepository repository;

    @Override
    public OrderItemResponse create(OrderItemRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public OrderItemResponse getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<OrderItemResponse> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public OrderItemResponse update(UUID id, OrderItemRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
