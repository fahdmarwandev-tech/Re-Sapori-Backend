package com.resapori.e_commerce.service.impl;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IRoleService;
import com.resapori.e_commerce.southbound.entity.Role;
import com.resapori.e_commerce.southbound.repository.IRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements IRoleService {

    private final IRoleRepository repository;

    @Override
    public Role create(Role entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Role getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Role> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Role update(UUID id, Role entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
