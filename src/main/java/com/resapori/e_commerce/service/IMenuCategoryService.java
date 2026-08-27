package com.resapori.e_commerce.service;

import com.resapori.e_commerce.southbound.entity.MenuCategory;
import java.util.List;
import java.util.UUID;

public interface IMenuCategoryService {
    MenuCategory create(MenuCategory entity);
    MenuCategory getById(UUID id);
    List<MenuCategory> getAll();
    MenuCategory update(UUID id, MenuCategory entity);
    void delete(UUID id);
}
