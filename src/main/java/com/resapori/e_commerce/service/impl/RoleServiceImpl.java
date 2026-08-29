package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.northbound.dto.role.RoleRequest;
import com.resapori.e_commerce.northbound.dto.role.RoleResponse;
import com.resapori.e_commerce.service.IRoleService;
import com.resapori.e_commerce.southbound.repository.IRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements IRoleService {

    private final IRoleRepository repository;

    @Override
    public RoleResponse create(RoleRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public RoleResponse getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<RoleResponse> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public RoleResponse update(UUID id, RoleRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
