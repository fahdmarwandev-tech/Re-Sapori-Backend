package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRedemptionResponse;

import java.util.List;
import java.util.UUID;

public interface IPromoCodeRedemptionService {
    PromoCodeRedemptionResponse getById(UUID id);
    List<PromoCodeRedemptionResponse> getAll();
    List<PromoCodeRedemptionResponse> getByUserId(UUID userId);
    List<PromoCodeRedemptionResponse> getByOrderId(UUID orderId);
}
