package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.northbound.dto.user.UserResponse;
import com.resapori.e_commerce.northbound.dto.user.UserUpdateRequest;
import com.resapori.e_commerce.service.IUserService;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements IUserService {

    private final IUserRepository repository;

    @Override
    public UserResponse getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<UserResponse> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UserResponse update(UUID id, UserUpdateRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
