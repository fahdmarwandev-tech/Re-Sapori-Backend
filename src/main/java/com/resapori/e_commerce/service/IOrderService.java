package com.resapori.e_commerce.service;

import com.resapori.e_commerce.southbound.entity.Order;
import java.util.List;
import java.util.UUID;

public interface IOrderService {
    Order create(Order entity);
    Order getById(UUID id);
    List<Order> getAll();
    Order update(UUID id, Order entity);
    void delete(UUID id);
}
