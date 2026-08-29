package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.order.OrderResponse;
import com.resapori.e_commerce.northbound.dto.order.PlaceOrderRequest;
import com.resapori.e_commerce.northbound.dto.order.UpdateOrderStatusRequest;
import com.resapori.e_commerce.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final IOrderService service;

    /** POST /api/orders — place a new order. */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.placeOrder(request));
    }

    /** GET /api/orders/{id} — get order details. */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /** GET /api/orders — list all orders (ADMIN / CASHIER). */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /** PATCH /api/orders/{id}/status — update order status (ADMIN / CASHIER). */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(id, request));
    }
}
