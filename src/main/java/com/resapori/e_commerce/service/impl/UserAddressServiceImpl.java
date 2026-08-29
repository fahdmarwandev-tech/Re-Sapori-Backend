package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.northbound.dto.address.AddressRequest;
import com.resapori.e_commerce.northbound.dto.address.AddressResponse;
import com.resapori.e_commerce.service.IUserAddressService;
import com.resapori.e_commerce.southbound.entity.User;
import com.resapori.e_commerce.southbound.entity.UserAddress;
import com.resapori.e_commerce.southbound.repository.IUserAddressRepository;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements IUserAddressService {

    private final IUserAddressRepository addressRepository;
    private final IUserRepository userRepository;

    @Override
    public List<AddressResponse> getAddresses(UUID userId) {
        requireUserExists(userId);
        return addressRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse addAddress(UUID userId, AddressRequest request) {
        User user = requireUserExists(userId);

        if (request.isDefault()) {
            addressRepository.clearDefaultsByUserId(userId);
        }

        UserAddress address = new UserAddress();
        address.setUser(user);
        applyRequest(address, request);

        return toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        requireUserExists(userId);
        UserAddress address = requireAddressOwnership(userId, addressId);

        if (request.isDefault()) {
            addressRepository.clearDefaultsByUserId(userId);
        }

        applyRequest(address, request);
        return toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        requireUserExists(userId);
        UserAddress address = requireAddressOwnership(userId, addressId);

        boolean wasDefault = address.isDefault();
        address.setActive(false);
        address.setDefault(false);
        addressRepository.save(address);

        // Auto-promote the next most-recently-created address as the new default
        if (wasDefault) {
            addressRepository.findByUserIdAndIsActiveTrue(userId)
                    .stream()
                    .max(Comparator.comparing(UserAddress::getCreatedAt))
                    .ifPresent(next -> {
                        next.setDefault(true);
                        addressRepository.save(next);
                    });
        }
    }

    @Override
    @Transactional
    public AddressResponse setDefault(UUID userId, UUID addressId) {
        requireUserExists(userId);
        UserAddress address = requireAddressOwnership(userId, addressId);

        addressRepository.clearDefaultsByUserId(userId);
        address.setDefault(true);
        return toResponse(addressRepository.save(address));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User requireUserExists(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserAddress requireAddressOwnership(UUID userId, UUID addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found for this user"));
    }

    private void applyRequest(UserAddress address, AddressRequest request) {
        address.setLabel(request.getLabel());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setFloor(request.getFloor());
        address.setApartment(request.getApartment());
        address.setLat(request.getLat());
        address.setLng(request.getLng());
        address.setDefault(request.isDefault());
    }

    private AddressResponse toResponse(UserAddress address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .street(address.getStreet())
                .city(address.getCity())
                .district(address.getDistrict())
                .floor(address.getFloor())
                .apartment(address.getApartment())
                .lat(address.getLat())
                .lng(address.getLng())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
