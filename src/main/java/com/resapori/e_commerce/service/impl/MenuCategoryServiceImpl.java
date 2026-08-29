package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryResponse;
import com.resapori.e_commerce.service.IMenuCategoryService;
import com.resapori.e_commerce.southbound.entity.MenuCategory;
import com.resapori.e_commerce.southbound.mapper.MenuCategoryMapper;
import com.resapori.e_commerce.southbound.repository.IMenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MenuCategoryServiceImpl implements IMenuCategoryService {

    private final IMenuCategoryRepository repository;
    private final MenuCategoryMapper mapper;

    @Override
    @Transactional
    public MenuCategoryResponse create(MenuCategoryRequest request) {
        MenuCategory entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuCategoryResponse getById(UUID id) {
        MenuCategory entity = findByIdOrThrow(id);
        if (!entity.isActive()) {
            throw new ResourceNotFoundException("MenuCategory not found with id: " + id);
        }
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getAll() {
        return repository.findAll().stream()
                .filter(MenuCategory::isActive)
                .sorted((a, b) -> {
                    Integer orderA = a.getDisplayOrder() != null ? a.getDisplayOrder() : Integer.MAX_VALUE;
                    Integer orderB = b.getDisplayOrder() != null ? b.getDisplayOrder() : Integer.MAX_VALUE;
                    return orderA.compareTo(orderB);
                })
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MenuCategoryResponse update(UUID id, MenuCategoryRequest request) {
        MenuCategory entity = findByIdOrThrow(id);
        entity.setNameEn(request.getNameEn());
        entity.setNameAr(request.getNameAr());
        entity.setDisplayOrder(request.getDisplayOrder());
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        MenuCategory entity = findByIdOrThrow(id);
        entity.setActive(false);
        repository.save(entity);
    }

    private MenuCategory findByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory not found with id: " + id));
    }
}
