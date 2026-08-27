package com.resapori.e_commerce.northbound.controllers;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IOrderItemService;
import com.resapori.e_commerce.southbound.entity.OrderItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final IOrderItemService service;

    @PostMapping
    public ResponseEntity<OrderItem> create(@RequestBody OrderItem entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItem> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderItem>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItem> update(@PathVariable UUID id, @RequestBody OrderItem entity) {
        return ResponseEntity.ok(service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
