package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.branch.BranchRequest;
import com.resapori.e_commerce.northbound.dto.branch.BranchResponse;

import java.util.List;
import java.util.UUID;

public interface IBranchService {
    BranchResponse create(BranchRequest request);
    BranchResponse getById(UUID id);
    List<BranchResponse> getAll();
    BranchResponse update(UUID id, BranchRequest request);
    void delete(UUID id);
}
