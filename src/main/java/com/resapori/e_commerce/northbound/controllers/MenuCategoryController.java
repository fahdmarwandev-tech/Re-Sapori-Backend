package com.resapori.e_commerce.northbound.controllers;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IMenuCategoryService;
import com.resapori.e_commerce.southbound.entity.MenuCategory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/menu-categories")
public class MenuCategoryController {

    private final IMenuCategoryService service;

    @PostMapping
    public ResponseEntity<MenuCategory> create(@RequestBody MenuCategory entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuCategory> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<MenuCategory>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuCategory> update(@PathVariable UUID id, @RequestBody MenuCategory entity) {
        return ResponseEntity.ok(service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
