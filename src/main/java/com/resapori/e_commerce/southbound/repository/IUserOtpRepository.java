package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserOtpRepository extends JpaRepository<UserOtp, UUID> {

    Optional<UserOtp> findByEmail(String email);

    Optional<UserOtp> findByOtpAndEmail(String otp, String email);

    Optional<UserOtp> findByResetToken(String resetToken);

    boolean existsByOtp(String otp);

    @Modifying
    @Query("DELETE FROM UserOtp u WHERE u.email = :email")
    void deleteAllByEmail(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM UserOtp u WHERE u.createdAt < :expiryDate")
    void deleteByCreatedAtBefore(@Param("expiryDate") LocalDateTime expiryDate);
}
