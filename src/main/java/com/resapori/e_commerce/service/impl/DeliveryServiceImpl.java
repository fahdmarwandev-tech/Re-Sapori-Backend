package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.northbound.dto.delivery.CalculateDeliveryFeeRequest;
import com.resapori.e_commerce.northbound.dto.delivery.DeliveryFeeResponse;
import com.resapori.e_commerce.service.IDeliveryService;
import com.resapori.e_commerce.southbound.entity.Branch;
import com.resapori.e_commerce.southbound.entity.UserAddress;
import com.resapori.e_commerce.southbound.repository.IBranchRepository;
import com.resapori.e_commerce.southbound.repository.IUserAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements IDeliveryService {

    public static final BigDecimal BASE_DISTANCE = BigDecimal.valueOf(4.0);
    public static final BigDecimal BASE_FEE = BigDecimal.valueOf(35.00);
    public static final BigDecimal RATE_PER_KM = BigDecimal.valueOf(7.00);

    // Default Re-Sapori kitchen hub coordinates (6th of October, Giza)
    public static final BigDecimal DEFAULT_RESTAURANT_LAT = BigDecimal.valueOf(29.996688);
    public static final BigDecimal DEFAULT_RESTAURANT_LNG = BigDecimal.valueOf(30.983078);
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final IBranchRepository branchRepository;
    private final IUserAddressRepository userAddressRepository;

    @Override
    public BigDecimal calculateDistanceKm(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double rLat1 = Math.toRadians(lat1.doubleValue());
        double rLat2 = Math.toRadians(lat2.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(rLat1) * Math.cos(rLat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateDeliveryFee(BigDecimal distanceKm) {
        if (distanceKm == null || distanceKm.compareTo(BigDecimal.ZERO) <= 0) {
            return BASE_FEE.setScale(2, RoundingMode.HALF_UP);
        }

        if (distanceKm.compareTo(BASE_DISTANCE) <= 0) {
            return BASE_FEE.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal excessDistance = distanceKm.subtract(BASE_DISTANCE);
        BigDecimal fee = BASE_FEE.add(RATE_PER_KM.multiply(excessDistance));
        // Round to nearest integer (e.g. 44.10 -> 44.00)
        return fee.setScale(0, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public DeliveryFeeResponse calculateDeliveryFee(CalculateDeliveryFeeRequest request) {
        BigDecimal customerLat = null;
        BigDecimal customerLng = null;

        // Resolve customer coordinates from addressId if provided
        if (request != null && request.getAddressId() != null) {
            UserAddress address = userAddressRepository.findById(request.getAddressId()).orElse(null);
            if (address != null && address.getLat() != null && address.getLng() != null) {
                customerLat = address.getLat();
                customerLng = address.getLng();
            }
        }

        // Fallback to explicit lat/lng from request
        if (customerLat == null && request != null && request.getLat() != null && request.getLng() != null) {
            customerLat = request.getLat();
            customerLng = request.getLng();
        }

        UUID preferredBranchId = request != null ? request.getBranchId() : null;
        Branch branch = resolveBranchForDelivery(customerLat, customerLng, preferredBranchId);

        // If no coordinates could be resolved, return standard base fee
        if (customerLat == null || customerLng == null) {
            return DeliveryFeeResponse.builder()
                    .distanceKm(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .deliveryFee(BASE_FEE.setScale(2, RoundingMode.HALF_UP))
                    .branchId(branch != null ? branch.getId() : null)
                    .branchName(branch != null ? branch.getName() : "Re-Sapori Main")
                    .currency("EGP")
                    .build();
        }

        BigDecimal branchLat = (branch != null && branch.getLat() != null) ? branch.getLat() : DEFAULT_RESTAURANT_LAT;
        BigDecimal branchLng = (branch != null && branch.getLng() != null) ? branch.getLng() : DEFAULT_RESTAURANT_LNG;

        BigDecimal distanceKm = calculateDistanceKm(customerLat, customerLng, branchLat, branchLng);
        BigDecimal fee = calculateDeliveryFee(distanceKm);

        return DeliveryFeeResponse.builder()
                .distanceKm(distanceKm)
                .deliveryFee(fee)
                .branchId(branch != null ? branch.getId() : null)
                .branchName(branch != null ? branch.getName() : "Re-Sapori Kitchen Hub")
                .currency("EGP")
                .build();
    }

    @Override
    public DeliveryFeeResponse calculateForAddress(UUID addressId, UUID preferredBranchId) {
        CalculateDeliveryFeeRequest req = CalculateDeliveryFeeRequest.builder()
                .addressId(addressId)
                .branchId(preferredBranchId)
                .build();
        return calculateDeliveryFee(req);
    }

    @Override
    public Branch resolveBranchForDelivery(BigDecimal customerLat, BigDecimal customerLng, UUID preferredBranchId) {
        List<Branch> activeBranches = branchRepository.findByIsActiveTrue();
        if (activeBranches.isEmpty()) {
            return null;
        }

        // 1. If customer requested a specific branch, prioritize it if active
        if (preferredBranchId != null) {
            for (Branch b : activeBranches) {
                if (b.getId().equals(preferredBranchId)) {
                    return b;
                }
            }
        }

        // 2. If customer has coordinates, find the closest active branch with valid coordinates
        if (customerLat != null && customerLng != null) {
            return activeBranches.stream()
                    .filter(b -> b.getLat() != null && b.getLng() != null)
                    .min(Comparator.comparing(b -> calculateDistanceKm(customerLat, customerLng, b.getLat(), b.getLng())))
                    .orElse(activeBranches.get(0));
        }

        // 3. Fallback to first active branch
        return activeBranches.get(0);
    }
}
