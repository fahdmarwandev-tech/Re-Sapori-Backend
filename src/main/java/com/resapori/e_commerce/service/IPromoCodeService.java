package com.resapori.e_commerce.service;

import com.resapori.e_commerce.southbound.entity.PromoCode;
import java.util.List;
import java.util.UUID;

public interface IPromoCodeService {
    PromoCode create(PromoCode entity);
    PromoCode getById(UUID id);
    List<PromoCode> getAll();
    PromoCode update(UUID id, PromoCode entity);
    void delete(UUID id);
}
