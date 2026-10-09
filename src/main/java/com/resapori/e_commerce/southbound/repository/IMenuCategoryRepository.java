package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IMenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {
    List<MenuCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<MenuCategory> findByIsActiveTrueAndIsVisibleTrueOrderByDisplayOrderAsc();

    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM MenuCategory c WHERE c.isActive = true")
    Integer findMaxDisplayOrder();
}
