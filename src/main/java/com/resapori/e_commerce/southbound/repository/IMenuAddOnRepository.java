package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.MenuAddOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IMenuAddOnRepository extends JpaRepository<MenuAddOn, UUID> {
    List<MenuAddOn> findByMenuItemId(UUID menuItemId);

    List<MenuAddOn> findByCategoryIdAndIsActiveTrue(UUID categoryId);

    @Query("SELECT a FROM MenuAddOn a JOIN FETCH a.category WHERE a.category.id IN :categoryIds AND a.isActive = true")
    List<MenuAddOn> findByCategoryIdInAndIsActiveTrue(@Param("categoryIds") Collection<UUID> categoryIds);
}
