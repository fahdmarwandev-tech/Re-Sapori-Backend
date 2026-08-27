package com.resapori.e_commerce.service;

import com.resapori.e_commerce.southbound.entity.Role;
import java.util.List;
import java.util.UUID;

public interface IRoleService {
    Role create(Role entity);
    Role getById(UUID id);
    List<Role> getAll();
    Role update(UUID id, Role entity);
    void delete(UUID id);
}
