package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.user.UserResponse;
import com.resapori.e_commerce.northbound.dto.user.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    UserResponse getById(UUID id);
    List<UserResponse> getAll();
    UserResponse update(UUID id, UserUpdateRequest request);
    void delete(UUID id);
}
