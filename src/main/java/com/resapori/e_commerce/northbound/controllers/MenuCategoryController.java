package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryResponse;
import com.resapori.e_commerce.service.IMenuCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/menu/categories")
public class MenuCategoryController {

    private final IMenuCategoryService service;

    @PostMapping
    public ResponseEntity<MenuCategoryResponse> create(@RequestBody @Valid MenuCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuCategoryResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<MenuCategoryResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuCategoryResponse> update(@PathVariable UUID id, @RequestBody @Valid MenuCategoryRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
