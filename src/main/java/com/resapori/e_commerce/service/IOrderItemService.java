package com.resapori.e_commerce.service;

import com.resapori.e_commerce.southbound.entity.OrderItem;
import java.util.List;
import java.util.UUID;

public interface IOrderItemService {
    OrderItem create(OrderItem entity);
    OrderItem getById(UUID id);
    List<OrderItem> getAll();
    OrderItem update(UUID id, OrderItem entity);
    void delete(UUID id);
}
