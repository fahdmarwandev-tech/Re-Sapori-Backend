package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IMenuItemRepository extends JpaRepository<MenuItem, UUID> {

    @Query("SELECT DISTINCT m FROM MenuItem m LEFT JOIN FETCH m.category LEFT JOIN FETCH m.addOns WHERE m.isActive = true")
    List<MenuItem> findAllActive();

    @Query("SELECT DISTINCT m FROM MenuItem m LEFT JOIN FETCH m.category LEFT JOIN FETCH m.addOns WHERE m.isActive = true AND m.isAvailable = true")
    List<MenuItem> findAllActiveAndAvailable();

    @Query("SELECT DISTINCT m FROM MenuItem m LEFT JOIN FETCH m.category LEFT JOIN FETCH m.addOns WHERE m.category.id = :categoryId AND m.isActive = true")
    List<MenuItem> findActiveByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT DISTINCT m FROM MenuItem m LEFT JOIN FETCH m.category LEFT JOIN FETCH m.addOns WHERE m.category.id = :categoryId AND m.isActive = true AND m.isAvailable = true")
    List<MenuItem> findActiveAndAvailableByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT m FROM MenuItem m LEFT JOIN FETCH m.category LEFT JOIN FETCH m.addOns WHERE m.id = :id AND m.isActive = true")
    Optional<MenuItem> findActiveById(@Param("id") UUID id);
}
