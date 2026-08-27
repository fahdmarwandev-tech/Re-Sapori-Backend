package com.resapori.e_commerce.service.impl;


import lombok.RequiredArgsConstructor;
import com.resapori.e_commerce.service.IPromoCodeService;
import com.resapori.e_commerce.southbound.entity.PromoCode;
import com.resapori.e_commerce.southbound.repository.IPromoCodeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PromoCodeServiceImpl implements IPromoCodeService {

    private final IPromoCodeRepository repository;

    @Override
    public PromoCode create(PromoCode entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PromoCode getById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<PromoCode> getAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PromoCode update(UUID id, PromoCode entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
