package com.resapori.e_commerce.service;

import com.resapori.e_commerce.southbound.entity.PromoCodeRedemption;
import java.util.List;
import java.util.UUID;

public interface IPromoCodeRedemptionService {
    PromoCodeRedemption create(PromoCodeRedemption entity);
    PromoCodeRedemption getById(UUID id);
    List<PromoCodeRedemption> getAll();
    PromoCodeRedemption update(UUID id, PromoCodeRedemption entity);
    void delete(UUID id);
}
