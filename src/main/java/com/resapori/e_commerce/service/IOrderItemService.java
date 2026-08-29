package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.order.OrderItemRequest;
import com.resapori.e_commerce.northbound.dto.order.OrderItemResponse;

import java.util.List;
import java.util.UUID;

public interface IOrderItemService {
    OrderItemResponse create(OrderItemRequest request);
    OrderItemResponse getById(UUID id);
    List<OrderItemResponse> getAll();
    OrderItemResponse update(UUID id, OrderItemRequest request);
    void delete(UUID id);
}
