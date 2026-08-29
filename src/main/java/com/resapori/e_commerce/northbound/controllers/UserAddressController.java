package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.address.AddressRequest;
import com.resapori.e_commerce.northbound.dto.address.AddressResponse;
import com.resapori.e_commerce.service.IUserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/{userId}/addresses")
public class UserAddressController {

    private final IUserAddressService service;

    /** GET /api/users/{userId}/addresses — list all active addresses for a user. */
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.getAddresses(userId));
    }

    /** POST /api/users/{userId}/addresses — add a new address. */
    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @PathVariable UUID userId,
            @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAddress(userId, request));
    }

    /** PUT /api/users/{userId}/addresses/{addressId} — update an existing address. */
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable UUID userId,
            @PathVariable UUID addressId,
            @RequestBody AddressRequest request) {
        return ResponseEntity.ok(service.updateAddress(userId, addressId, request));
    }

    /** DELETE /api/users/{userId}/addresses/{addressId} — soft-delete an address. */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID userId,
            @PathVariable UUID addressId) {
        service.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/users/{userId}/addresses/{addressId}/default — set as default. */
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefault(
            @PathVariable UUID userId,
            @PathVariable UUID addressId) {
        return ResponseEntity.ok(service.setDefault(userId, addressId));
    }
}
