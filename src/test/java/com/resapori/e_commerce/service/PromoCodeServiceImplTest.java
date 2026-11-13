package com.resapori.e_commerce.service;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRequest;
import com.resapori.e_commerce.northbound.dto.promo.PromoCodeResponse;
import com.resapori.e_commerce.service.impl.PromoCodeServiceImpl;
import com.resapori.e_commerce.southbound.entity.PromoCode;
import com.resapori.e_commerce.southbound.enums.DiscountType;
import com.resapori.e_commerce.southbound.mapper.PromoCodeMapper;
import com.resapori.e_commerce.southbound.repository.IMenuItemRepository;
import com.resapori.e_commerce.southbound.repository.IPromoCodeRepository;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceImplTest {

    @Mock
    private IPromoCodeRepository repository;

    @Mock
    private IMenuItemRepository menuItemRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private PromoCodeMapper mapper;

    @InjectMocks
    private PromoCodeServiceImpl promoCodeService;

    private PromoCode testPromo;
    private PromoCodeResponse testResponse;

    @BeforeEach
    void setUp() {
        testPromo = new PromoCode();
        testPromo.setCode("SAPORI10");
        testPromo.setDiscountType(DiscountType.PERCENTAGE);
        testPromo.setDiscountValue(BigDecimal.valueOf(10.00));
        testPromo.setCurrentUses(0);
        testPromo.setMaxUses(100);
        testPromo.setMaxUsesPerUser(1);
        testPromo.setActive(true);

        testResponse = PromoCodeResponse.builder()
                .code("SAPORI10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10.00))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("validateCode: Success when promo code is active, not expired, under limit")
    void validateCode_Success() {
        when(repository.findByCodeIgnoreCaseAndIsActiveTrue("SAPORI10")).thenReturn(Optional.of(testPromo));
        when(mapper.toResponse(testPromo)).thenReturn(testResponse);

        PromoCodeResponse result = promoCodeService.validateCode("SAPORI10");

        assertNotNull(result);
        assertEquals("SAPORI10", result.getCode());
        verify(repository).findByCodeIgnoreCaseAndIsActiveTrue("SAPORI10");
    }

    @Test
    @DisplayName("validateCode: Throws ResourceNotFoundException when code not found")
    void validateCode_NotFound() {
        when(repository.findByCodeIgnoreCaseAndIsActiveTrue("INVALID")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> promoCodeService.validateCode("INVALID"));
    }

    @Test
    @DisplayName("validateCode: Throws IllegalArgumentException when code is expired")
    void validateCode_Expired() {
        testPromo.setExpiryDate(LocalDateTime.now().minusDays(1));
        when(repository.findByCodeIgnoreCaseAndIsActiveTrue("EXPIRED")).thenReturn(Optional.of(testPromo));

        assertThrows(IllegalArgumentException.class, () -> promoCodeService.validateCode("EXPIRED"));
    }

    @Test
    @DisplayName("validateCode: Throws IllegalArgumentException when max uses reached")
    void validateCode_MaxUsesReached() {
        testPromo.setMaxUses(5);
        testPromo.setCurrentUses(5);
        when(repository.findByCodeIgnoreCaseAndIsActiveTrue("CAPPED")).thenReturn(Optional.of(testPromo));

        assertThrows(IllegalArgumentException.class, () -> promoCodeService.validateCode("CAPPED"));
    }

    @Test
    @DisplayName("create: Saves and returns promo code when no active code with same name exists")
    void create_Success() {
        PromoCodeRequest request = PromoCodeRequest.builder()
                .code("NEW20")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(20))
                .build();

        when(repository.existsByCodeIgnoreCaseAndIsActiveTrue("NEW20")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(testPromo);
        when(repository.save(testPromo)).thenReturn(testPromo);
        when(mapper.toResponse(testPromo)).thenReturn(testResponse);

        PromoCodeResponse result = promoCodeService.create(request);
        assertNotNull(result);
        assertTrue(testPromo.isActive());
        verify(repository).existsByCodeIgnoreCaseAndIsActiveTrue("NEW20");
        verify(repository).save(testPromo);
    }

    @Test
    @DisplayName("create: Throws IllegalArgumentException when an active promo code with same name exists")
    void create_ActiveDuplicateThrows() {
        PromoCodeRequest request = PromoCodeRequest.builder()
                .code("sapori10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))
                .build();

        when(repository.existsByCodeIgnoreCaseAndIsActiveTrue("SAPORI10")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> promoCodeService.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("getAll: Returns only active promo codes")
    void getAll_ReturnsOnlyActive() {
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(testPromo));
        when(mapper.toResponseList(List.of(testPromo))).thenReturn(List.of(testResponse));

        List<PromoCodeResponse> result = promoCodeService.getAll();

        assertEquals(1, result.size());
        verify(repository).findByIsActiveTrueOrderByCreatedAtDesc();
        verify(repository, never()).findAll();
    }

    @Test
    @DisplayName("getById: Throws ResourceNotFoundException when promo code is inactive")
    void getById_InactiveThrows() {
        UUID id = UUID.randomUUID();
        testPromo.setId(id);
        testPromo.setActive(false);
        when(repository.findById(id)).thenReturn(Optional.of(testPromo));

        assertThrows(ResourceNotFoundException.class, () -> promoCodeService.getById(id));
    }

    @Test
    @DisplayName("delete: Marks promo code inactive and saves")
    void delete_MarksInactive() {
        UUID id = UUID.randomUUID();
        testPromo.setId(id);
        testPromo.setActive(true);
        when(repository.findById(id)).thenReturn(Optional.of(testPromo));

        promoCodeService.delete(id);

        assertFalse(testPromo.isActive());
        verify(repository).save(testPromo);
    }
}
