package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.common.security.AuthUtil;
import com.resapori.e_commerce.northbound.dto.user.UserResponse;
import com.resapori.e_commerce.northbound.dto.user.UserUpdateRequest;
import com.resapori.e_commerce.service.IUserService;
import com.resapori.e_commerce.southbound.entity.Branch;
import com.resapori.e_commerce.southbound.entity.Role;
import com.resapori.e_commerce.southbound.entity.User;
import com.resapori.e_commerce.southbound.mapper.UserMapper;
import com.resapori.e_commerce.southbound.repository.IBranchRepository;
import com.resapori.e_commerce.southbound.repository.IRoleRepository;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class UserServiceImpl implements IUserService {

    private final IUserRepository repository;
    private final IRoleRepository roleRepository;
    private final IBranchRepository branchRepository;
    private final UserMapper userMapper;
    private final AuthUtil authUtil;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        validateSelfOrAdmin(user);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe() {
        User authUser = authUtil.getAuthenticatedUser();
        if (authUser == null) {
            throw new AccessDeniedException("Must be logged in to view profile");
        }
        return userMapper.toResponse(authUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userMapper.toResponseList(repository.findAll());
    }

    @Override
    public UserResponse update(UUID id, UserUpdateRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        validateSelfOrAdmin(user);

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        // Branch and Role updates can only be performed by ADMIN
        if (authUtil.hasRole("ROLE_ADMIN")) {
            if (request.getBranchId() != null) {
                Branch branch = branchRepository.findById(request.getBranchId())
                        .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + request.getBranchId()));
                user.setBranch(branch);
            }

            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                Set<Role> roles = roleRepository.findByNameIn(request.getRoles());
                if (!roles.isEmpty()) {
                    user.setRoles(new HashSet<>(roles));
                }
            }
        }

        User saved = repository.save(user);
        return userMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(false);
        repository.save(user);
    }

    private void validateSelfOrAdmin(User targetUser) {
        if (authUtil.hasRole("ROLE_ADMIN")) {
            return;
        }
        User authUser = authUtil.getAuthenticatedUser();
        if (authUser == null || !authUser.getId().equals(targetUser.getId())) {
            throw new AccessDeniedException("Cannot access other users' profile");
        }
    }
}
