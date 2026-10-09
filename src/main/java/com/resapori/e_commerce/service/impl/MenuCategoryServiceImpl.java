package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryResponse;
import com.resapori.e_commerce.service.IMenuCategoryService;
import com.resapori.e_commerce.southbound.entity.MenuCategory;
import com.resapori.e_commerce.southbound.mapper.MenuCategoryMapper;
import com.resapori.e_commerce.southbound.repository.IMenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MenuCategoryServiceImpl implements IMenuCategoryService {

    private final IMenuCategoryRepository repository;
    private final MenuCategoryMapper mapper;

    @Override
    @Transactional
    public MenuCategoryResponse create(MenuCategoryRequest request) {
        MenuCategory entity = mapper.toEntity(request);
        if (request.getIsVisible() != null) {
            entity.setVisible(request.getIsVisible());
        }

        List<MenuCategory> existing = new ArrayList<>(repository.findByIsActiveTrueOrderByDisplayOrderAsc());

        Integer requestedOrder = request.getDisplayOrder();
        if (requestedOrder == null || requestedOrder <= 0 || requestedOrder > existing.size() + 1) {
            existing.add(entity);
        } else {
            int insertIndex = Math.max(0, requestedOrder - 1);
            existing.add(insertIndex, entity);
        }

        // Re-index all active categories sequentially (1, 2, 3...)
        for (int i = 0; i < existing.size(); i++) {
            existing.get(i).setDisplayOrder(i + 1);
        }

        repository.saveAll(existing);
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
        List<MenuCategory> categories = isAdmin()
                ? repository.findByIsActiveTrueOrderByDisplayOrderAsc()
                : repository.findByIsActiveTrueAndIsVisibleTrueOrderByDisplayOrderAsc();
        return mapper.toResponseList(categories);
    }

    @Override
    @Transactional
    public MenuCategoryResponse update(UUID id, MenuCategoryRequest request) {
        MenuCategory entity = findByIdOrThrow(id);
        entity.setNameEn(request.getNameEn());
        entity.setNameAr(request.getNameAr());
        entity.setSubtitleEn(request.getSubtitleEn());
        entity.setSubtitleAr(request.getSubtitleAr());
        if (request.getIsVisible() != null) {
            entity.setVisible(request.getIsVisible());
        }

        Integer newOrder = request.getDisplayOrder();
        List<MenuCategory> existing = new ArrayList<>(repository.findByIsActiveTrueOrderByDisplayOrderAsc());
        existing.removeIf(c -> c.getId().equals(id));

        if (newOrder == null || newOrder <= 0 || newOrder > existing.size() + 1) {
            existing.add(entity);
        } else {
            int targetIndex = Math.max(0, newOrder - 1);
            existing.add(targetIndex, entity);
        }

        // Re-index all active categories sequentially
        for (int i = 0; i < existing.size(); i++) {
            existing.get(i).setDisplayOrder(i + 1);
        }

        repository.saveAll(existing);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public MenuCategoryResponse toggleVisibility(UUID id, boolean visible) {
        MenuCategory entity = findByIdOrThrow(id);
        entity.setVisible(visible);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        MenuCategory entity = findByIdOrThrow(id);
        entity.setActive(false);
        repository.save(entity);

        // Normalize remaining active categories sequence
        List<MenuCategory> remaining = repository.findByIsActiveTrueOrderByDisplayOrderAsc();
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setDisplayOrder(i + 1);
        }
        repository.saveAll(remaining);
    }

    private MenuCategory findByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory not found with id: " + id));
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
