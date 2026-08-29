package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.role.RoleRequest;
import com.resapori.e_commerce.northbound.dto.role.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface IRoleService {
    RoleResponse create(RoleRequest request);
    RoleResponse getById(UUID id);
    List<RoleResponse> getAll();
    RoleResponse update(UUID id, RoleRequest request);
    void delete(UUID id);
}
