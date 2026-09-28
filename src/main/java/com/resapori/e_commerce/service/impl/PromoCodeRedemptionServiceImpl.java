package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRedemptionResponse;
import com.resapori.e_commerce.service.IPromoCodeRedemptionService;
import com.resapori.e_commerce.southbound.mapper.PromoCodeRedemptionMapper;
import com.resapori.e_commerce.southbound.repository.IPromoCodeRedemptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PromoCodeRedemptionServiceImpl implements IPromoCodeRedemptionService {

    private final IPromoCodeRedemptionRepository repository;
    private final PromoCodeRedemptionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PromoCodeRedemptionResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("PromoCodeRedemption not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromoCodeRedemptionResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromoCodeRedemptionResponse> getByUserId(UUID userId) {
        return mapper.toResponseList(repository.findByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromoCodeRedemptionResponse> getByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId)
                .map(r -> List.of(mapper.toResponse(r)))
                .orElseGet(List::of);
    }
}
