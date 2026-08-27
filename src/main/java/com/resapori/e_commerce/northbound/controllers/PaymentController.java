package com.resapori.e_commerce.northbound.controllers;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IPaymentService;
import com.resapori.e_commerce.southbound.entity.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final IPaymentService service;

    @PostMapping
    public ResponseEntity<Payment> create(@RequestBody Payment entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> update(@PathVariable UUID id, @RequestBody Payment entity) {
        return ResponseEntity.ok(service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
