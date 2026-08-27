package com.resapori.e_commerce.service;

import com.resapori.e_commerce.southbound.entity.Branch;
import java.util.List;
import java.util.UUID;

public interface IBranchService {
    Branch create(Branch entity);
    Branch getById(UUID id);
    List<Branch> getAll();
    Branch update(UUID id, Branch entity);
    void delete(UUID id);
}
