package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.northbound.dto.menu.MenuAddOnRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuItemRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuItemResponse;
import com.resapori.e_commerce.service.IMenuItemService;
import com.resapori.e_commerce.southbound.entity.MenuAddOn;
import com.resapori.e_commerce.southbound.entity.MenuCategory;
import com.resapori.e_commerce.southbound.entity.MenuItem;
import com.resapori.e_commerce.southbound.mapper.MenuAddOnMapper;
import com.resapori.e_commerce.southbound.mapper.MenuItemMapper;
import com.resapori.e_commerce.southbound.repository.IMenuCategoryRepository;
import com.resapori.e_commerce.southbound.repository.IMenuItemRepository;
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
public class MenuItemServiceImpl implements IMenuItemService {

    private final IMenuItemRepository repository;
    private final IMenuCategoryRepository categoryRepository;
    private final MenuItemMapper mapper;
    private final MenuAddOnMapper addOnMapper;

    @Override
    @Transactional
    public MenuItemResponse create(MenuItemRequest request) {
        MenuCategory category = findCategoryOrThrow(request.getCategoryId());
        MenuItem entity = mapper.toEntity(request);
        entity.setCategory(category);

        // Map addOns and wire back the menuItem reference
        if (request.getAddOns() != null && !request.getAddOns().isEmpty()) {
            List<MenuAddOn> addOns = mapAddOns(request.getAddOns(), entity);
            entity.getAddOns().addAll(addOns);
        }

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getById(UUID id) {
        MenuItem item = repository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with id: " + id));
        return mapper.toResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAll() {
        List<MenuItem> items = isAdmin()
                ? repository.findAllActive()
                : repository.findAllActiveAndAvailable();
        return mapper.toResponseList(items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getByCategory(UUID categoryId) {
        List<MenuItem> items = isAdmin()
                ? repository.findActiveByCategoryId(categoryId)
                : repository.findActiveAndAvailableByCategoryId(categoryId);
        return mapper.toResponseList(items);
    }

    @Override
    @Transactional
    public MenuItemResponse update(UUID id, MenuItemRequest request) {
        MenuItem entity = findByIdOrThrow(id);
        applyBasicFields(entity, request);
        applyPriceAndFlags(entity, request);
        applyCategorization(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    private void applyBasicFields(MenuItem entity, MenuItemRequest req) {
        entity.setNameEn(req.getNameEn());
        entity.setNameAr(req.getNameAr());
        entity.setDescriptionEn(req.getDescriptionEn());
        entity.setDescriptionAr(req.getDescriptionAr());
        entity.setImageUrl(req.getImageUrl());
    }

    private void applyPriceAndFlags(MenuItem entity, MenuItemRequest req) {
        entity.setCurrentPrice(req.getCurrentPrice());
        entity.setOriginalPrice(req.getOriginalPrice());
        entity.setDiscountPrice(req.getDiscountPrice());
        entity.setMiniPrice(req.getMiniPrice());
        entity.setAvailable(Boolean.TRUE.equals(req.getAvailable()));
        entity.setStock(req.getStock());
    }

    private void applyCategorization(MenuItem entity, MenuItemRequest req) {
        entity.setCategory(findCategoryOrThrow(req.getCategoryId()));
        entity.getAddOns().clear();
        if (req.getAddOns() != null && !req.getAddOns().isEmpty()) {
            entity.getAddOns().addAll(mapAddOns(req.getAddOns(), entity));
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        MenuItem entity = findByIdOrThrow(id);
        entity.setActive(false);
        repository.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<MenuAddOn> mapAddOns(List<MenuAddOnRequest> requests, MenuItem owner) {
        List<MenuAddOn> result = new ArrayList<>();
        for (MenuAddOnRequest req : requests) {
            MenuAddOn addOn = addOnMapper.toEntity(req);
            addOn.setMenuItem(owner);
            result.add(addOn);
        }
        return result;
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
