package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.payment.PaymentResponse;
import com.resapori.e_commerce.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final IPaymentService service;

    /** GET /api/payments/{id} — get a payment record by ID (ADMIN). */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /** GET /api/payments — list all payment records (ADMIN). */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /** GET /api/payments/by-order/{orderId} — get payment history for an order (ADMIN). */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(service.getByOrderId(orderId));
    }
}
