package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IMenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {
}
