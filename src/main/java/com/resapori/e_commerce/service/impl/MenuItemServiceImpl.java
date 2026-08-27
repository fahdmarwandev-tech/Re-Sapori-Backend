package com.resapori.e_commerce.service.impl;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IMenuItemService;
import com.resapori.e_commerce.southbound.entity.MenuItem;
import com.resapori.e_commerce.southbound.repository.IMenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MenuItemServiceImpl implements IMenuItemService {

    private final IMenuItemRepository repository;

    @Override
    public MenuItem create(MenuItem entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public MenuItem getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<MenuItem> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public MenuItem update(UUID id, MenuItem entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
