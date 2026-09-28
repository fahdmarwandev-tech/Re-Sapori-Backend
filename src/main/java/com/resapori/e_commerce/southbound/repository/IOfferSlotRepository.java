package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.OfferSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IOfferSlotRepository extends JpaRepository<OfferSlot, UUID> {
    List<OfferSlot> findByOfferIdOrderByDisplayOrderAsc(UUID offerId);
}
