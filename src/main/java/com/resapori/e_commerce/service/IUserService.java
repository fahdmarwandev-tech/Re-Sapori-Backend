package com.resapori.e_commerce.service;

import com.resapori.e_commerce.southbound.entity.User;
import java.util.List;
import java.util.UUID;

public interface IUserService {
    User create(User entity);
    User getById(UUID id);
    List<User> getAll();
    User update(UUID id, User entity);
    void delete(UUID id);
}
