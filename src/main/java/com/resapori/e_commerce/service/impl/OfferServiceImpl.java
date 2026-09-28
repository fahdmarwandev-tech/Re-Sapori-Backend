package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.northbound.dto.offer.OfferRequest;
import com.resapori.e_commerce.northbound.dto.offer.OfferResponse;
import com.resapori.e_commerce.northbound.dto.offer.OfferSlotRequest;
import com.resapori.e_commerce.service.IOfferService;
import com.resapori.e_commerce.southbound.entity.MenuCategory;
import com.resapori.e_commerce.southbound.entity.MenuItem;
import com.resapori.e_commerce.southbound.entity.Offer;
import com.resapori.e_commerce.southbound.entity.OfferSlot;
import com.resapori.e_commerce.southbound.enums.DiscountTarget;
import com.resapori.e_commerce.southbound.mapper.OfferMapper;
import com.resapori.e_commerce.southbound.repository.IMenuCategoryRepository;
import com.resapori.e_commerce.southbound.repository.IMenuItemRepository;
import com.resapori.e_commerce.southbound.repository.IOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OfferServiceImpl implements IOfferService {

    private final IOfferRepository offerRepository;
    private final IMenuCategoryRepository categoryRepository;
    private final IMenuItemRepository menuItemRepository;
    private final OfferMapper offerMapper;

    @Override
    @Transactional
    public OfferResponse create(OfferRequest request) {
        Offer offer = offerMapper.toEntity(request);
        if (request.getDiscountTarget() != DiscountTarget.FIXED_PRICE) {
            offer.setFixedPrice(null);
        }
        if (request.getCategoryId() != null) {
            offer.setCategory(findCategoryOrThrow(request.getCategoryId()));
        }
        if (request.getSlots() != null) {
            offer.setSlots(buildSlots(request.getSlots(), offer));
        }
        return offerMapper.toResponse(offerRepository.save(offer));
    }

    @Override
    @Transactional(readOnly = true)
    public OfferResponse getById(UUID id) {
        Offer offer = offerRepository.findByIdWithSlots(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + id));
        return offerMapper.toResponse(offer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferResponse> getAllActive() {
        return offerMapper.toResponseList(offerRepository.findAllActiveWithSlots());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferResponse> getAll() {
        return offerMapper.toResponseList(offerRepository.findAllWithSlots());
    }

    @Override
    @Transactional
    public OfferResponse update(UUID id, OfferRequest request) {
        Offer offer = offerRepository.findByIdWithSlots(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + id));
        applyOfferDetails(offer, request);
        applyOfferSlots(offer, request);
        return offerMapper.toResponse(offerRepository.save(offer));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + id));
        offer.setActive(false);
        offerRepository.save(offer);
    }

    // ─── Private Helpers (each <= 20 lines) ───────────────────────────

    private void applyOfferDetails(Offer offer, OfferRequest req) {
        offer.setNameEn(req.getNameEn());
        offer.setNameAr(req.getNameAr());
        offer.setDescriptionEn(req.getDescriptionEn());
        offer.setDescriptionAr(req.getDescriptionAr());
        offer.setImageUrl(req.getImageUrl());
        offer.setDiscountTarget(req.getDiscountTarget());
        offer.setDiscountPercentage(req.getDiscountPercentage());
        offer.setFixedPrice(req.getDiscountTarget() == DiscountTarget.FIXED_PRICE ? req.getFixedPrice() : null);
        offer.setBuyQuantity(req.getBuyQuantity() != null ? req.getBuyQuantity() : 1);
        offer.setGetQuantity(req.getGetQuantity() != null ? req.getGetQuantity() : 0);
        if (req.getCategoryId() != null) {
            offer.setCategory(findCategoryOrThrow(req.getCategoryId()));
        }
    }

    private void applyOfferSlots(Offer offer, OfferRequest req) {
        if (req.getSlots() == null) return;
        offer.getSlots().clear();
        offer.getSlots().addAll(buildSlots(req.getSlots(), offer));
    }

    private List<OfferSlot> buildSlots(List<OfferSlotRequest> slotRequests, Offer offer) {
        List<OfferSlot> slots = new ArrayList<>();
        for (OfferSlotRequest slotReq : slotRequests) {
            slots.add(buildSingleSlot(slotReq, offer));
        }
        return slots;
    }

    private OfferSlot buildSingleSlot(OfferSlotRequest req, Offer offer) {
        OfferSlot slot = new OfferSlot();
        slot.setOffer(offer);
        slot.setSlotNameEn(req.getSlotNameEn());
        slot.setSlotNameAr(req.getSlotNameAr());
        slot.setQuantity(req.getQuantity() != null ? req.getQuantity() : 1);
        slot.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        slot.setFree(Boolean.TRUE.equals(req.getIsFree()));
        attachSlotAssociations(slot, req);
        return slot;
    }

    private void attachSlotAssociations(OfferSlot slot, OfferSlotRequest req) {
        if (req.getMenuItemId() != null) {
            slot.setMenuItem(findMenuItemOrThrow(req.getMenuItemId()));
        }
        if (req.getCategoryId() != null) {
            slot.setCategory(findCategoryOrThrow(req.getCategoryId()));
        }
        if (req.getEligibleItemIds() != null && !req.getEligibleItemIds().isEmpty()) {
            slot.setEligibleItems(menuItemRepository.findAllById(req.getEligibleItemIds()));
        }
    }

    private MenuCategory findCategoryOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    private MenuItem findMenuItemOrThrow(UUID id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
    }
}
