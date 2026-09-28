package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.offer.OfferRequest;
import com.resapori.e_commerce.northbound.dto.offer.OfferResponse;

import java.util.List;
import java.util.UUID;

public interface IOfferService {
    OfferResponse create(OfferRequest request);
    OfferResponse getById(UUID id);
    List<OfferResponse> getAllActive();
    List<OfferResponse> getAll();
    OfferResponse update(UUID id, OfferRequest request);
    void delete(UUID id);
}
