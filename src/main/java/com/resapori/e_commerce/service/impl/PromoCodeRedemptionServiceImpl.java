package com.resapori.e_commerce.service.impl;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IPromoCodeRedemptionService;
import com.resapori.e_commerce.southbound.entity.PromoCodeRedemption;
import com.resapori.e_commerce.southbound.repository.IPromoCodeRedemptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PromoCodeRedemptionServiceImpl implements IPromoCodeRedemptionService {

    private final IPromoCodeRedemptionRepository repository;

    @Override
    public PromoCodeRedemption create(PromoCodeRedemption entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PromoCodeRedemption getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<PromoCodeRedemption> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PromoCodeRedemption update(UUID id, PromoCodeRedemption entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
