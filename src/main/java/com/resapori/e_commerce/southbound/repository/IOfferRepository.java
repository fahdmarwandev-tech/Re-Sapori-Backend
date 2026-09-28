package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IOfferRepository extends JpaRepository<Offer, UUID> {

    @Query("SELECT DISTINCT o FROM Offer o LEFT JOIN FETCH o.category LEFT JOIN FETCH o.slots s LEFT JOIN FETCH s.menuItem LEFT JOIN FETCH s.category WHERE o.isActive = true ORDER BY o.createdAt DESC")
    List<Offer> findAllActiveWithSlots();

    @Query("SELECT DISTINCT o FROM Offer o LEFT JOIN FETCH o.category LEFT JOIN FETCH o.slots s LEFT JOIN FETCH s.menuItem LEFT JOIN FETCH s.category ORDER BY o.createdAt DESC")
    List<Offer> findAllWithSlots();

    @Query("SELECT o FROM Offer o LEFT JOIN FETCH o.category LEFT JOIN FETCH o.slots s LEFT JOIN FETCH s.menuItem LEFT JOIN FETCH s.category WHERE o.id = :id AND o.isActive = true")
    Optional<Offer> findActiveByIdWithSlots(@Param("id") UUID id);

    @Query("SELECT o FROM Offer o LEFT JOIN FETCH o.category LEFT JOIN FETCH o.slots s LEFT JOIN FETCH s.menuItem LEFT JOIN FETCH s.category WHERE o.id = :id")
    Optional<Offer> findByIdWithSlots(@Param("id") UUID id);
}
