package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.common.security.AuthUtil;
import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRequest;
import com.resapori.e_commerce.northbound.dto.promo.PromoCodeResponse;
import com.resapori.e_commerce.northbound.dto.promo.WelcomeEligibilityResponse;
import com.resapori.e_commerce.service.IPromoCodeService;
import com.resapori.e_commerce.southbound.entity.PromoCode;
import com.resapori.e_commerce.southbound.entity.User;
import com.resapori.e_commerce.southbound.mapper.PromoCodeMapper;
import com.resapori.e_commerce.southbound.repository.IMenuItemRepository;
import com.resapori.e_commerce.southbound.repository.IOrderRepository;
import com.resapori.e_commerce.southbound.repository.IPromoCodeRedemptionRepository;
import com.resapori.e_commerce.southbound.repository.IPromoCodeRepository;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PromoCodeServiceImpl implements IPromoCodeService {

    private static final String WELCOME_PROMO_CODE = "WELCOME10";

    private final IPromoCodeRepository repository;
    private final IMenuItemRepository menuItemRepository;
    private final IUserRepository userRepository;
    private final IOrderRepository orderRepository;
    private final IPromoCodeRedemptionRepository promoCodeRedemptionRepository;
    private final AuthUtil authUtil;
    private final PromoCodeMapper mapper;

    @Override
    @Transactional
    public PromoCodeResponse create(PromoCodeRequest request) {
        if (request.getCode() == null || request.getCode().trim().isBlank()) {
            throw new IllegalArgumentException("Promo code cannot be empty");
        }
        String cleanCode = request.getCode().trim().toUpperCase();
        if (repository.existsByCodeIgnoreCaseAndIsActiveTrue(cleanCode)) {
            throw new IllegalArgumentException("Promo code already exists: " + cleanCode);
        }
        request.setCode(cleanCode);
        PromoCode promo = mapper.toEntity(request);
        promo.setCode(cleanCode);
        promo.setActive(true);
        attachAssociations(promo, request);
        return mapper.toResponse(repository.save(promo));
    }

    @Override
    @Transactional(readOnly = true)
    public PromoCodeResponse getById(UUID id) {
        return mapper.toResponse(findActiveByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromoCodeResponse> getAll() {
        return mapper.toResponseList(repository.findByIsActiveTrueOrderByCreatedAtDesc());
    }

    @Override
    @Transactional
    public PromoCodeResponse update(UUID id, PromoCodeRequest request) {
        PromoCode promo = findActiveByIdOrThrow(id);
        applyUpdates(promo, request);
        attachAssociations(promo, request);
        return mapper.toResponse(repository.save(promo));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PromoCode promo = findActiveByIdOrThrow(id);
        promo.setActive(false);
        repository.save(promo);
    }

    @Override
    @Transactional(readOnly = true)
    public PromoCodeResponse validateCode(String code) {
        if (code == null || code.trim().isBlank()) {
            throw new IllegalArgumentException("Promo code cannot be empty");
        }
        User user = authUtil != null ? authUtil.getAuthenticatedUser() : null;
        if (RequestContextHolder.getRequestAttributes() != null && user == null) {
            throw new IllegalArgumentException("Please sign in or sign up to use promo codes");
        }
        String cleanCode = code.trim().toUpperCase();
        PromoCode promo = repository.findByCodeIgnoreCaseAndIsActiveTrue(cleanCode)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found or inactive: " + cleanCode));
        validateRules(promo);
        if (user != null && WELCOME_PROMO_CODE.equalsIgnoreCase(cleanCode) && !isUserEligibleForWelcome(user, promo)) {
            throw new IllegalArgumentException("Welcome discount is only valid for your first order");
        }
        return mapper.toResponse(promo);
    }

    @Override
    @Transactional(readOnly = true)
    public WelcomeEligibilityResponse checkWelcomeEligibility() {
        User user = authUtil != null ? authUtil.getAuthenticatedUser() : null;
        if (user == null) {
            return WelcomeEligibilityResponse.builder().eligible(false).build();
        }
        PromoCode promo = repository.findByCodeIgnoreCaseAndIsActiveTrue(WELCOME_PROMO_CODE).orElse(null);
        if (promo == null || !isPromoCurrentlyUsable(promo) || !isUserEligibleForWelcome(user, promo)) {
            return WelcomeEligibilityResponse.builder().eligible(false).build();
        }
        BigDecimal pct = promo.getDiscountValue() != null ? promo.getDiscountValue() : BigDecimal.valueOf(10);
        return WelcomeEligibilityResponse.builder()
                .eligible(true)
                .code(promo.getCode())
                .discountPercentage(pct)
                .build();
    }

    // ─── Private Helpers (each <= 20 lines) ───────────────────────────

    private boolean isPromoCurrentlyUsable(PromoCode promo) {
        if (promo.getExpiryDate() != null && promo.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        return promo.getMaxUses() == null || promo.getCurrentUses() < promo.getMaxUses();
    }

    private boolean isUserEligibleForWelcome(User user, PromoCode promo) {
        if (orderRepository != null && orderRepository.countCompletedOrActiveOrdersByUserId(user.getId()) > 0) {
            return false;
        }
        if (promoCodeRedemptionRepository != null) {
            long redemptions = promoCodeRedemptionRepository.countByPromoCodeIdAndUserId(promo.getId(), user.getId());
            if (redemptions >= promo.getMaxUsesPerUser()) {
                return false;
            }
        }
        return true;
    }

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
            String cleanCode = req.getCode().trim().toUpperCase();
            if (repository.existsByCodeIgnoreCaseAndIsActiveTrueAndIdNot(cleanCode, promo.getId())) {
                throw new IllegalArgumentException("Promo code already exists: " + cleanCode);
            }
            promo.setCode(cleanCode);
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

    private PromoCode findActiveByIdOrThrow(UUID id) {
        PromoCode promo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromoCode not found with id: " + id));
        if (!promo.isActive()) {
            throw new ResourceNotFoundException("PromoCode not found with id: " + id);
        }
        return promo;
    }
}
