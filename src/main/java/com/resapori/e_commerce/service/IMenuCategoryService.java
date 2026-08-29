package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryResponse;

import java.util.List;
import java.util.UUID;

public interface IMenuCategoryService {
    MenuCategoryResponse create(MenuCategoryRequest request);
    MenuCategoryResponse getById(UUID id);
    List<MenuCategoryResponse> getAll();
    MenuCategoryResponse update(UUID id, MenuCategoryRequest request);
    void delete(UUID id);
}
