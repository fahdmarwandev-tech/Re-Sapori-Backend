package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRequest;
import com.resapori.e_commerce.northbound.dto.promo.PromoCodeResponse;

import java.util.List;
import java.util.UUID;

public interface IPromoCodeService {
    PromoCodeResponse create(PromoCodeRequest request);
    PromoCodeResponse getById(UUID id);
    List<PromoCodeResponse> getAll();
    PromoCodeResponse update(UUID id, PromoCodeRequest request);
    void delete(UUID id);
}
