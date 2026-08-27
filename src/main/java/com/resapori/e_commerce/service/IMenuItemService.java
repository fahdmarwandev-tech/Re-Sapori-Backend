package com.resapori.e_commerce.service;

import com.resapori.e_commerce.southbound.entity.MenuItem;
import java.util.List;
import java.util.UUID;

public interface IMenuItemService {
    MenuItem create(MenuItem entity);
    MenuItem getById(UUID id);
    List<MenuItem> getAll();
    MenuItem update(UUID id, MenuItem entity);
    void delete(UUID id);
}
