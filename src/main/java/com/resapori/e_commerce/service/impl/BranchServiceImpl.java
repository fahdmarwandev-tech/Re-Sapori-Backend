package com.resapori.e_commerce.service.impl;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IBranchService;
import com.resapori.e_commerce.southbound.entity.Branch;
import com.resapori.e_commerce.southbound.repository.IBranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BranchServiceImpl implements IBranchService {

    private final IBranchRepository repository;

    @Override
    public Branch create(Branch entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Branch getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Branch> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Branch update(UUID id, Branch entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
