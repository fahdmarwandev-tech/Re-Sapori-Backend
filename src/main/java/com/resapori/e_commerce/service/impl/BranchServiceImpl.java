package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.northbound.dto.branch.BranchRequest;
import com.resapori.e_commerce.northbound.dto.branch.BranchResponse;
import com.resapori.e_commerce.service.IBranchService;
import com.resapori.e_commerce.southbound.entity.Branch;
import com.resapori.e_commerce.southbound.mapper.BranchMapper;
import com.resapori.e_commerce.southbound.repository.IBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BranchServiceImpl implements IBranchService {

    private final IBranchRepository repository;
    private final BranchMapper mapper;

    @Override
    @Transactional
    public BranchResponse create(BranchRequest request) {
        Branch entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getById(UUID id) {
        Branch entity = findByIdOrThrow(id);
        if (!entity.isActive()) {
            throw new ResourceNotFoundException("Branch not found with id: " + id);
        }
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAll() {
        return repository.findAll().stream()
                .filter(Branch::isActive)
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BranchResponse update(UUID id, BranchRequest request) {
        Branch entity = findByIdOrThrow(id);
        entity.setName(request.getName());
        entity.setAddress(request.getAddress());
        entity.setPhoneNumber(request.getPhoneNumber());
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Branch entity = findByIdOrThrow(id);
        entity.setActive(false);
        repository.save(entity);
    }

    private Branch findByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
    }
}
