package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IOrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrderId(UUID orderId);

    @Query("SELECT oi FROM OrderItem oi LEFT JOIN FETCH oi.menuItem WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderIdWithMenuItem(@Param("orderId") UUID orderId);

    @Query("SELECT oi FROM OrderItem oi LEFT JOIN FETCH oi.menuItem WHERE oi.order.id IN :orderIds")
    List<OrderItem> findByOrderIdInWithMenuItem(@Param("orderIds") List<UUID> orderIds);
}
