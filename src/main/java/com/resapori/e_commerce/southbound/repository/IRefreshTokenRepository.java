package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.RefreshToken;
import com.resapori.e_commerce.southbound.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
