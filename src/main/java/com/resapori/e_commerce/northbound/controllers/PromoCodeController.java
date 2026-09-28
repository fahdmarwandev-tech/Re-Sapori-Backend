package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRequest;
import com.resapori.e_commerce.northbound.dto.promo.PromoCodeResponse;
import com.resapori.e_commerce.service.IPromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/promo-codes")
public class PromoCodeController {

    private final IPromoCodeService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PromoCodeResponse> create(@RequestBody PromoCodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PromoCodeResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/validate")
    public ResponseEntity<PromoCodeResponse> validateCode(@RequestParam String code) {
        return ResponseEntity.ok(service.validateCode(code));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> update(@PathVariable UUID id, @RequestBody PromoCodeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
