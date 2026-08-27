package com.resapori.e_commerce.northbound.controllers;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IPromoCodeRedemptionService;
import com.resapori.e_commerce.southbound.entity.PromoCodeRedemption;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/promo-code-redemptions")
public class PromoCodeRedemptionController {

    private final IPromoCodeRedemptionService service;

    @PostMapping
    public ResponseEntity<PromoCodeRedemption> create(@RequestBody PromoCodeRedemption entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeRedemption> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PromoCodeRedemption>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeRedemption> update(@PathVariable UUID id, @RequestBody PromoCodeRedemption entity) {
        return ResponseEntity.ok(service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
