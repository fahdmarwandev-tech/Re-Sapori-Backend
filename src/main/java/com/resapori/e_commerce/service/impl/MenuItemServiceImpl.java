package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.northbound.dto.menu.MenuItemRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuItemResponse;
import com.resapori.e_commerce.service.IMenuItemService;
import com.resapori.e_commerce.southbound.entity.MenuCategory;
import com.resapori.e_commerce.southbound.entity.MenuItem;
import com.resapori.e_commerce.southbound.mapper.MenuItemMapper;
import com.resapori.e_commerce.southbound.repository.IMenuCategoryRepository;
import com.resapori.e_commerce.southbound.repository.IMenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MenuItemServiceImpl implements IMenuItemService {

    private final IMenuItemRepository repository;
    private final IMenuCategoryRepository categoryRepository;
    private final MenuItemMapper mapper;

    @Override
    @Transactional
    public MenuItemResponse create(MenuItemRequest request) {
        MenuCategory category = findCategoryOrThrow(request.getCategoryId());
        MenuItem entity = mapper.toEntity(request);
        entity.setCategory(category);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getById(UUID id) {
        MenuItem item = findByIdOrThrow(id);
        if (!item.isActive()) {
            throw new ResourceNotFoundException("MenuItem not found with id: " + id);
        }
        return mapper.toResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAll() {
        boolean isAdmin = isAdmin();
        return repository.findAll().stream()
                .filter(MenuItem::isActive)
                .filter(item -> isAdmin || item.isAvailable())
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getByCategory(UUID categoryId) {
        boolean isAdmin = isAdmin();
        return repository.findByCategoryId(categoryId).stream()
                .filter(MenuItem::isActive)
                .filter(item -> isAdmin || item.isAvailable())
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MenuItemResponse update(UUID id, MenuItemRequest request) {
        MenuItem entity = findByIdOrThrow(id);
        MenuCategory category = findCategoryOrThrow(request.getCategoryId());
        
        entity.setCategory(category);
        entity.setNameEn(request.getNameEn());
        entity.setNameAr(request.getNameAr());
        entity.setDescriptionEn(request.getDescriptionEn());
        entity.setDescriptionAr(request.getDescriptionAr());
        entity.setCurrentPrice(request.getCurrentPrice());
        entity.setImageUrl(request.getImageUrl());
        entity.setAvailable(request.isAvailable());
        entity.setStock(request.getStock());
        
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        MenuItem entity = findByIdOrThrow(id);
        entity.setActive(false);
        repository.save(entity);
    }

    private MenuItem findByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with id: " + id));
    }

    private MenuCategory findCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory not found with id: " + categoryId));
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
