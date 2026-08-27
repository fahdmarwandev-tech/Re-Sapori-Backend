package com.resapori.e_commerce.service.impl;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IMenuCategoryService;
import com.resapori.e_commerce.southbound.entity.MenuCategory;
import com.resapori.e_commerce.southbound.repository.IMenuCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MenuCategoryServiceImpl implements IMenuCategoryService {

    private final IMenuCategoryRepository repository;

    @Override
    public MenuCategory create(MenuCategory entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public MenuCategory getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<MenuCategory> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public MenuCategory update(UUID id, MenuCategory entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
