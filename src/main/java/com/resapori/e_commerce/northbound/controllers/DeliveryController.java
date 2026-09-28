package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.delivery.CalculateDeliveryFeeRequest;
import com.resapori.e_commerce.northbound.dto.delivery.DeliveryFeeResponse;
import com.resapori.e_commerce.service.IDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final IDeliveryService deliveryService;

    /**
     * POST /api/delivery/calculate
     * Real-time calculation of distance, delivery fee, and fulfilling branch based on customer coordinates or address.
     * Publicly accessible so checkout and address selection can preview fees immediately.
     */
    @PostMapping("/calculate")
    public ResponseEntity<DeliveryFeeResponse> calculateDeliveryFee(
            @RequestBody CalculateDeliveryFeeRequest request) {
        return ResponseEntity.ok(deliveryService.calculateDeliveryFee(request));
    }
}
