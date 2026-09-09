package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.common.security.CustomUserDetails;
import com.resapori.e_commerce.northbound.dto.address.AddressRequest;
import com.resapori.e_commerce.northbound.dto.address.AddressResponse;
import com.resapori.e_commerce.service.IUserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user/addresses")
public class UserAddressController {

    private final IUserAddressService service;

    /** GET /api/user/addresses — list all active addresses for a user. */
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(service.getAddresses(userDetails.getUser().getId()));
    }

    /** POST /api/user/addresses — add a new address. */
    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAddress(userDetails.getUser().getId(), request));
    }

    /** PUT /api/user/addresses/{addressId} — update an existing address. */
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID addressId,
            @RequestBody AddressRequest request) {
        return ResponseEntity.ok(service.updateAddress(userDetails.getUser().getId(), addressId, request));
    }

    /** DELETE /api/user/addresses/{addressId} — soft-delete an address. */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID addressId) {
        service.deleteAddress(userDetails.getUser().getId(), addressId);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/user/addresses/{addressId}/default — set as default. */
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefault(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID addressId) {
        return ResponseEntity.ok(service.setDefault(userDetails.getUser().getId(), addressId));
    }
}
