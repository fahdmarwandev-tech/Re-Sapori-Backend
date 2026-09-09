package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.MenuAddOn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IMenuAddOnRepository extends JpaRepository<MenuAddOn, UUID> {
    List<MenuAddOn> findByMenuItemId(UUID menuItemId);
}
