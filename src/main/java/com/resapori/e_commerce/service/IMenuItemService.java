package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.menu.MenuItemRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuItemResponse;

import java.util.List;
import java.util.UUID;

public interface IMenuItemService {
    MenuItemResponse create(MenuItemRequest request);
    MenuItemResponse getById(UUID id);
    List<MenuItemResponse> getAll();
    List<MenuItemResponse> getByCategory(UUID categoryId);
    MenuItemResponse update(UUID id, MenuItemRequest request);
    void delete(UUID id);
}
