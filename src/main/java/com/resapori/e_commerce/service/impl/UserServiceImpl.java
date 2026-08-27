package com.resapori.e_commerce.service.impl;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IUserService;
import com.resapori.e_commerce.southbound.entity.User;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements IUserService {

    private final IUserRepository repository;

    @Override
    public User create(User entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public User getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<User> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public User update(UUID id, User entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
