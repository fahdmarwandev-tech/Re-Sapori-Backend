package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.order.OrderResponse;
import com.resapori.e_commerce.northbound.dto.order.PlaceOrderRequest;
import com.resapori.e_commerce.northbound.dto.order.UpdateOrderStatusRequest;

import java.util.List;
import java.util.UUID;

public interface IOrderService {
    OrderResponse placeOrder(PlaceOrderRequest request);
    OrderResponse getById(UUID id);
    List<OrderResponse> getAll();
    List<OrderResponse> getMyOrders();
    OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request);
    void delete(UUID id);
}
