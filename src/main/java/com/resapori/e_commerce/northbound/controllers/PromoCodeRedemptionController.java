package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRedemptionResponse;
import com.resapori.e_commerce.service.IPromoCodeRedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/promo-code-redemptions")
public class PromoCodeRedemptionController {

    private final IPromoCodeRedemptionService service;

    /** GET /api/promo-code-redemptions/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeRedemptionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /** GET /api/promo-code-redemptions — list all (ADMIN). */
    @GetMapping
    public ResponseEntity<List<PromoCodeRedemptionResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /** GET /api/promo-code-redemptions/by-user/{userId} — list a user's redemption history. */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<PromoCodeRedemptionResponse>> getByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    /** GET /api/promo-code-redemptions/by-order/{orderId} — get redemption for a specific order. */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<PromoCodeRedemptionResponse>> getByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(service.getByOrderId(orderId));
    }
}
