package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IMenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {
    @Query("SELECT DISTINCT c FROM MenuCategory c LEFT JOIN FETCH c.addOns WHERE c.isActive = true ORDER BY c.displayOrder ASC")
    List<MenuCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    @Query("SELECT DISTINCT c FROM MenuCategory c LEFT JOIN FETCH c.addOns WHERE c.isActive = true AND c.isVisible = true ORDER BY c.displayOrder ASC")
    List<MenuCategory> findByIsActiveTrueAndIsVisibleTrueOrderByDisplayOrderAsc();

    @Query("SELECT c FROM MenuCategory c LEFT JOIN FETCH c.addOns WHERE c.id = :id")
    Optional<MenuCategory> findByIdWithAddOns(@Param("id") UUID id);

    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM MenuCategory c WHERE c.isActive = true")
    Integer findMaxDisplayOrder();
}
