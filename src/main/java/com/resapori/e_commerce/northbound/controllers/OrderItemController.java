package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.order.OrderItemRequest;
import com.resapori.e_commerce.northbound.dto.order.OrderItemResponse;
import com.resapori.e_commerce.service.IOrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final IOrderItemService service;

    @GetMapping("/{id}")
    public ResponseEntity<OrderItemResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderItemResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
