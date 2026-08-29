package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.address.AddressRequest;
import com.resapori.e_commerce.northbound.dto.address.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface IUserAddressService {

    /** Returns all active addresses for the given user. */
    List<AddressResponse> getAddresses(UUID userId);

    /** Adds a new address for the user. If isDefault is true, clears other defaults first. */
    AddressResponse addAddress(UUID userId, AddressRequest request);

    /** Updates an existing address. Re-enforces default uniqueness if isDefault is true. */
    AddressResponse updateAddress(UUID userId, UUID addressId, AddressRequest request);

    /**
     * Soft-deletes an address. If it was the default, auto-promotes the most recently
     * created remaining active address as the new default.
     */
    void deleteAddress(UUID userId, UUID addressId);

    /** Sets a specific address as the user's default, clearing all others. */
    AddressResponse setDefault(UUID userId, UUID addressId);
}
