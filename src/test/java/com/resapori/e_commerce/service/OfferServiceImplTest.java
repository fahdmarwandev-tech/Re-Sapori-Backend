package com.resapori.e_commerce.service;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.northbound.dto.offer.OfferRequest;
import com.resapori.e_commerce.northbound.dto.offer.OfferResponse;
import com.resapori.e_commerce.northbound.dto.offer.OfferSlotRequest;
import com.resapori.e_commerce.service.impl.OfferServiceImpl;
import com.resapori.e_commerce.southbound.entity.Offer;
import com.resapori.e_commerce.southbound.enums.DiscountTarget;
import com.resapori.e_commerce.southbound.mapper.OfferMapper;
import com.resapori.e_commerce.southbound.repository.IMenuCategoryRepository;
import com.resapori.e_commerce.southbound.repository.IMenuItemRepository;
import com.resapori.e_commerce.southbound.repository.IOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceImplTest {

    @Mock
    private IOfferRepository offerRepository;
    @Mock
    private IMenuCategoryRepository categoryRepository;
    @Mock
    private IMenuItemRepository menuItemRepository;
    @Mock
    private OfferMapper offerMapper;

    @InjectMocks
    private OfferServiceImpl offerService;

    private Offer testOffer;
    private OfferResponse testResponse;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testOffer = new Offer();
        testOffer.setId(testId);
        testOffer.setNameEn("Weekend Pizza Deal");
        testOffer.setNameAr("عرض الويك إند");
        testOffer.setDiscountTarget(DiscountTarget.CHEAPEST_ITEM);
        testOffer.setDiscountPercentage(BigDecimal.valueOf(100.00));
        testOffer.setBuyQuantity(1);
        testOffer.setGetQuantity(1);
        testOffer.setSlots(new ArrayList<>());

        testResponse = OfferResponse.builder()
                .id(testId)
                .nameEn("Weekend Pizza Deal")
                .discountTarget(DiscountTarget.CHEAPEST_ITEM)
                .discountPercentage(BigDecimal.valueOf(100.00))
                .buyQuantity(1)
                .getQuantity(1)
                .build();
    }

    @Test
    @DisplayName("create: Saves and returns offer response")
    void create_Success() {
        OfferRequest request = OfferRequest.builder()
                .nameEn("Weekend Pizza Deal")
                .nameAr("عرض الويك إند")
                .discountTarget(DiscountTarget.CHEAPEST_ITEM)
                .discountPercentage(BigDecimal.valueOf(100.00))
                .buyQuantity(1)
                .getQuantity(1)
                .slots(List.of(
                        OfferSlotRequest.builder().slotNameEn("Pizza 1").slotNameAr("بيتزا ١").build(),
                        OfferSlotRequest.builder().slotNameEn("Pizza 2").slotNameAr("بيتزا ٢").build()
                ))
                .build();

        when(offerMapper.toEntity(request)).thenReturn(testOffer);
        when(offerRepository.save(any(Offer.class))).thenReturn(testOffer);
        when(offerMapper.toResponse(testOffer)).thenReturn(testResponse);

        OfferResponse result = offerService.create(request);

        assertNotNull(result);
        assertEquals("Weekend Pizza Deal", result.getNameEn());
        verify(offerRepository).save(any(Offer.class));
    }

    @Test
    @DisplayName("getById: Returns offer when found")
    void getById_Success() {
        when(offerRepository.findByIdWithSlots(testId)).thenReturn(Optional.of(testOffer));
        when(offerMapper.toResponse(testOffer)).thenReturn(testResponse);

        OfferResponse result = offerService.getById(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        verify(offerRepository).findByIdWithSlots(testId);
    }

    @Test
    @DisplayName("getById: Throws ResourceNotFoundException when not found")
    void getById_NotFound() {
        UUID unknownId = UUID.randomUUID();
        when(offerRepository.findByIdWithSlots(unknownId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> offerService.getById(unknownId));
    }

    @Test
    @DisplayName("getAllActive: Returns active offers")
    void getAllActive_Success() {
        when(offerRepository.findAllActiveWithSlots()).thenReturn(List.of(testOffer));
        when(offerMapper.toResponseList(anyList())).thenReturn(List.of(testResponse));

        List<OfferResponse> result = offerService.getAllActive();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(offerRepository).findAllActiveWithSlots();
    }

    @Test
    @DisplayName("delete: Sets isActive to false")
    void delete_Success() {
        when(offerRepository.findById(testId)).thenReturn(Optional.of(testOffer));
        when(offerRepository.save(testOffer)).thenReturn(testOffer);

        offerService.delete(testId);

        assertFalse(testOffer.isActive());
        verify(offerRepository).save(testOffer);
    }
}
