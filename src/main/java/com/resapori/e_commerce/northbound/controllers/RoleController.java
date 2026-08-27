package com.resapori.e_commerce.northbound.controllers;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IRoleService;
import com.resapori.e_commerce.southbound.entity.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final IRoleService service;

    @PostMapping
    public ResponseEntity<Role> create(@RequestBody Role entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> update(@PathVariable UUID id, @RequestBody Role entity) {
        return ResponseEntity.ok(service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
