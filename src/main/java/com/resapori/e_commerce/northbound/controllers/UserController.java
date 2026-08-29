package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.user.UserResponse;
import com.resapori.e_commerce.northbound.dto.user.UserUpdateRequest;
import com.resapori.e_commerce.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final IUserService service;

    /** GET /api/users/{id} — get user profile (ADMIN or own user). */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /** GET /api/users — list all users (ADMIN only). */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /** PUT /api/users/{id} — update user profile (own user or ADMIN). */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    /** DELETE /api/users/{id} — deactivate user account (ADMIN only). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
