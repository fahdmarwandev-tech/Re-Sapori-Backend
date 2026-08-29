package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRequest;
import com.resapori.e_commerce.northbound.dto.promo.PromoCodeResponse;
import com.resapori.e_commerce.service.IPromoCodeService;
import com.resapori.e_commerce.southbound.repository.IPromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PromoCodeServiceImpl implements IPromoCodeService {

    private final IPromoCodeRepository repository;

    @Override
    public PromoCodeResponse create(PromoCodeRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PromoCodeResponse getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<PromoCodeResponse> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PromoCodeResponse update(UUID id, PromoCodeRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
