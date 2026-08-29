package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRedemptionResponse;
import com.resapori.e_commerce.service.IPromoCodeRedemptionService;
import com.resapori.e_commerce.southbound.repository.IPromoCodeRedemptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PromoCodeRedemptionServiceImpl implements IPromoCodeRedemptionService {

    private final IPromoCodeRedemptionRepository repository;

    @Override
    public PromoCodeRedemptionResponse getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<PromoCodeRedemptionResponse> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<PromoCodeRedemptionResponse> getByUserId(UUID userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<PromoCodeRedemptionResponse> getByOrderId(UUID orderId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
