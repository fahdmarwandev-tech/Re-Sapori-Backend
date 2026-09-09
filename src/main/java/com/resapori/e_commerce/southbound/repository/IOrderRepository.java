package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IOrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
    List<Order> findByBranchId(UUID branchId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.branch WHERE o.isActive = true ORDER BY o.createdAt DESC")
    List<Order> findAllWithUserAndBranch();

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.branch WHERE o.id = :id AND o.isActive = true")
    Optional<Order> findByIdWithUserAndBranch(@Param("id") UUID id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.branch WHERE o.user.id = :userId AND o.isActive = true ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithUserAndBranch(@Param("userId") UUID userId);
}
