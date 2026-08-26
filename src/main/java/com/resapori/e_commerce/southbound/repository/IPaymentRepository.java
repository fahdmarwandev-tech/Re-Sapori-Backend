package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.Payment;
import com.resapori.e_commerce.southbound.enums.PaymentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymobTransactionId(String paymobTransactionId);

    List<Payment> findByOrderId(UUID orderId);

    List<Payment> findByOrderIdAndStatus(UUID orderId, PaymentStatus status);
}
