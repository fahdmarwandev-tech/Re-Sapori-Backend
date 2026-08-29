package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.northbound.dto.order.OrderResponse;
import com.resapori.e_commerce.northbound.dto.order.PlaceOrderRequest;
import com.resapori.e_commerce.northbound.dto.order.UpdateOrderStatusRequest;
import com.resapori.e_commerce.service.IOrderService;
import com.resapori.e_commerce.southbound.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository repository;

    @Override
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public OrderResponse getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<OrderResponse> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
