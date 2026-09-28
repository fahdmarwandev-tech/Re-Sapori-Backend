package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRequest;
import com.resapori.e_commerce.northbound.dto.promo.PromoCodeResponse;
import com.resapori.e_commerce.service.IPromoCodeService;
import com.resapori.e_commerce.southbound.entity.PromoCode;
import com.resapori.e_commerce.southbound.mapper.PromoCodeMapper;
import com.resapori.e_commerce.southbound.repository.IMenuItemRepository;
import com.resapori.e_commerce.southbound.repository.IPromoCodeRepository;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PromoCodeServiceImpl implements IPromoCodeService {

    private final IPromoCodeRepository repository;
    private final IMenuItemRepository menuItemRepository;
    private final IUserRepository userRepository;
    private final PromoCodeMapper mapper;

    @Override
    @Transactional
    public PromoCodeResponse create(PromoCodeRequest request) {
        if (request.getCode() == null || request.getCode().trim().isBlank()) {
            throw new IllegalArgumentException("Promo code cannot be empty");
        }
        String cleanCode = request.getCode().trim().toUpperCase();
        if (repository.existsByCodeIgnoreCase(cleanCode)) {
            throw new IllegalArgumentException("Promo code already exists: " + cleanCode);
        }
        request.setCode(cleanCode);
        PromoCode promo = mapper.toEntity(request);
        promo.setCode(cleanCode);
        attachAssociations(promo, request);
        return mapper.toResponse(repository.save(promo));
    }

    @Override
    @Transactional(readOnly = true)
    public PromoCodeResponse getById(UUID id) {
        return mapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromoCodeResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    @Transactional
    public PromoCodeResponse update(UUID id, PromoCodeRequest request) {
        PromoCode promo = findByIdOrThrow(id);
        applyUpdates(promo, request);
        attachAssociations(promo, request);
        return mapper.toResponse(repository.save(promo));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PromoCode promo = findByIdOrThrow(id);
        promo.setActive(false);
        repository.save(promo);
    }

    @Override
    @Transactional(readOnly = true)
    public PromoCodeResponse validateCode(String code) {
        if (code == null || code.trim().isBlank()) {
            throw new IllegalArgumentException("Promo code cannot be empty");
        }
        String cleanCode = code.trim().toUpperCase();
        PromoCode promo = repository.findByCodeIgnoreCaseAndIsActiveTrue(cleanCode)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found or inactive: " + cleanCode));
        validateRules(promo);
        return mapper.toResponse(promo);
    }

    // ─── Private Helpers (each <= 20 lines) ───────────────────────────

    private void validateRules(PromoCode promo) {
        if (promo.getExpiryDate() != null && promo.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Promo code has expired");
        }
        if (promo.getMaxUses() != null && promo.getCurrentUses() >= promo.getMaxUses()) {
            throw new IllegalArgumentException("Promo code usage limit has been reached");
        }
    }

    private void applyUpdates(PromoCode promo, PromoCodeRequest req) {
        if (req.getCode() != null && !req.getCode().trim().isBlank()) {
            promo.setCode(req.getCode().trim().toUpperCase());
        }
        promo.setDiscountType(req.getDiscountType());
        promo.setDiscountValue(req.getDiscountValue());
        promo.setExpiryDate(req.getExpiryDate());
        promo.setMaxUses(req.getMaxUses());
        if (req.getMaxUsesPerUser() > 0) {
            promo.setMaxUsesPerUser(req.getMaxUsesPerUser());
        }
    }

    private void attachAssociations(PromoCode promo, PromoCodeRequest req) {
        if (req.getFreeItemId() != null) {
            promo.setFreeItem(menuItemRepository.findById(req.getFreeItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Free item not found")));
        }
        if (req.getUserId() != null) {
            promo.setUser(userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found")));
        }
    }

    private PromoCode findByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromoCode not found with id: " + id));
    }
}
