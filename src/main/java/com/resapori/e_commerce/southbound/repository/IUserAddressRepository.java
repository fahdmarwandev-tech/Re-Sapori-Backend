package com.resapori.e_commerce.southbound.repository;

import com.resapori.e_commerce.southbound.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserAddressRepository extends JpaRepository<UserAddress, UUID> {

    /** Returns all active addresses for a given user. */
    List<UserAddress> findByUserIdAndIsActiveTrue(UUID userId);

    /** Finds an address that belongs to a specific user (guards against cross-user access). */
    Optional<UserAddress> findByIdAndUserId(UUID id, UUID userId);

    /** Unsets the default flag on all addresses for a user before setting a new default. */
    @Modifying
    @Transactional
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultsByUserId(@Param("userId") UUID userId);
}
