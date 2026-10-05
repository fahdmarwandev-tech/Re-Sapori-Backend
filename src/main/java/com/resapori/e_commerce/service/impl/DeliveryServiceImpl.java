package com.resapori.e_commerce.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resapori.e_commerce.northbound.dto.delivery.CalculateDeliveryFeeRequest;
import com.resapori.e_commerce.northbound.dto.delivery.DeliveryFeeResponse;
import com.resapori.e_commerce.northbound.dto.delivery.DeliveryZoneDto;
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
import java.util.Collections;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // If no active branch exists at all
        if (branch == null || !branch.isActive()) {
            return DeliveryFeeResponse.builder()
                    .distanceKm(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .deliveryFee(BigDecimal.ZERO)
                    .branchId(null)
                    .branchName("No Active Branch")
                    .currency("EGP")
                    .isCovered(false)
                    .build();
        }

        // If no coordinates could be resolved, return standard base fee
        if (customerLat == null || customerLng == null) {
            return DeliveryFeeResponse.builder()
                    .distanceKm(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .deliveryFee(BASE_FEE.setScale(2, RoundingMode.HALF_UP))
                    .branchId(branch.getId())
                    .branchName(branch.getName())
                    .currency("EGP")
                    .isCovered(true)
                    .build();
        }

        BigDecimal branchLat = (branch.getLat() != null) ? branch.getLat() : DEFAULT_RESTAURANT_LAT;
        BigDecimal branchLng = (branch.getLng() != null) ? branch.getLng() : DEFAULT_RESTAURANT_LNG;

        BigDecimal distanceKm = calculateDistanceKm(customerLat, customerLng, branchLat, branchLng);

        // Check if customer falls into any delivery zone for the fulfilling branch
        DeliveryZoneDto matchedZone = (branch != null) ? findMatchingZone(branch, customerLat, customerLng) : null;
        boolean isCovered = (matchedZone != null);

        // Zone-specific fee override or standard distance fee
        BigDecimal fee;
        if (!isCovered) {
            fee = BigDecimal.ZERO;
        } else if (matchedZone != null && matchedZone.getDeliveryFee() != null) {
            fee = matchedZone.getDeliveryFee();
        } else {
            fee = calculateDeliveryFee(distanceKm);
        }

        return DeliveryFeeResponse.builder()
                .distanceKm(distanceKm)
                .deliveryFee(fee)
                .branchId(branch != null ? branch.getId() : null)
                .branchName(branch != null ? branch.getName() : "Re-Sapori Kitchen Hub")
                .currency("EGP")
                .isCovered(isCovered)
                .zoneName(matchedZone != null ? matchedZone.getName() : null)
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

        // 1. If customer coordinates are present, prioritize branches whose delivery zones contain the coordinates
        if (customerLat != null && customerLng != null) {
            // A. Check if requested preferred branch covers the customer
            if (preferredBranchId != null) {
                for (Branch b : activeBranches) {
                    if (b.getId().equals(preferredBranchId)) {
                        if (findMatchingZone(b, customerLat, customerLng) != null) {
                            return b;
                        }
                    }
                }
            }

            // B. Find all active branches that cover this customer
            List<Branch> coveringBranches = activeBranches.stream()
                    .filter(b -> findMatchingZone(b, customerLat, customerLng) != null)
                    .toList();

            if (!coveringBranches.isEmpty()) {
                // Return the closest covering branch
                return coveringBranches.stream()
                        .filter(b -> b.getLat() != null && b.getLng() != null)
                        .min(Comparator.comparing(b -> calculateDistanceKm(customerLat, customerLng, b.getLat(), b.getLng())))
                        .orElse(coveringBranches.get(0));
            }

            // C. Fallback: closest branch by distance even if technically outside configured zones
            return activeBranches.stream()
                    .filter(b -> b.getLat() != null && b.getLng() != null)
                    .min(Comparator.comparing(b -> calculateDistanceKm(customerLat, customerLng, b.getLat(), b.getLng())))
                    .orElse(activeBranches.get(0));
        }

        // 2. No coordinates provided: prioritize preferred branch if active
        if (preferredBranchId != null) {
            for (Branch b : activeBranches) {
                if (b.getId().equals(preferredBranchId)) {
                    return b;
                }
            }
        }

        // 3. Fallback to first active branch
        return activeBranches.get(0);
    }

    /**
     * Checks if a customer coordinate is covered by any active delivery zone (radius or polygon) in a branch.
     * If the branch has no zones configured, defaults to a 15km circular radius around the branch location.
     */
    public DeliveryZoneDto findMatchingZone(Branch branch, BigDecimal customerLat, BigDecimal customerLng) {
        if (branch == null || !branch.isActive() || customerLat == null || customerLng == null) {
            return null;
        }

        List<DeliveryZoneDto> zones = parseZones(branch.getDeliveryZones());
        if (zones.isEmpty()) {
            // Check if any active branch in the system has zones configured
            List<Branch> allActive = branchRepository.findByIsActiveTrue();
            boolean anyBranchHasZones = allActive.stream()
                    .anyMatch(b -> !parseZones(b.getDeliveryZones()).isEmpty());

            // Only fallback to 15km default radius if NO branch in the entire system has zones configured yet
            if (!anyBranchHasZones && branch.getLat() != null && branch.getLng() != null) {
                BigDecimal dist = calculateDistanceKm(customerLat, customerLng, branch.getLat(), branch.getLng());
                if (dist.doubleValue() <= 15.0) {
                    return DeliveryZoneDto.builder()
                            .name("Default Hub Area")
                            .type("RADIUS")
                            .radiusKm(15.0)
                            .build();
                }
            }
            return null;
        }

        for (DeliveryZoneDto zone : zones) {
            if (Boolean.FALSE.equals(zone.getIsActive())) {
                continue;
            }

            if ("POLYGON".equalsIgnoreCase(zone.getType())) {
                if (zone.getCoordinates() != null && zone.getCoordinates().size() >= 3) {
                    if (isPointInPolygon(customerLat.doubleValue(), customerLng.doubleValue(), zone.getCoordinates())) {
                        return zone;
                    }
                }
            } else {
                // RADIUS
                BigDecimal cLat = zone.getCenterLat() != null ? zone.getCenterLat() : branch.getLat();
                BigDecimal cLng = zone.getCenterLng() != null ? zone.getCenterLng() : branch.getLng();
                double maxRadius = zone.getRadiusKm() != null ? zone.getRadiusKm() : 8.0;

                if (cLat != null && cLng != null) {
                    BigDecimal dist = calculateDistanceKm(customerLat, customerLng, cLat, cLng);
                    if (dist.doubleValue() <= maxRadius) {
                        return zone;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Standard Ray-Casting algorithm to check if a (lat, lng) point is inside a 2D polygon.
     */
    public static boolean isPointInPolygon(double testLat, double testLng, List<List<Double>> points) {
        if (points == null || points.size() < 3) return false;
        boolean inside = false;
        int n = points.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double latI = points.get(i).get(0);
            double lngI = points.get(i).get(1);
            double latJ = points.get(j).get(0);
            double lngJ = points.get(j).get(1);

            boolean intersect = ((latI > testLat) != (latJ > testLat))
                    && (testLng < (lngJ - lngI) * (testLat - latI) / (latJ - latI) + lngI);
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }

    private List<DeliveryZoneDto> parseZones(String zonesJson) {
        if (zonesJson == null || zonesJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(zonesJson, new TypeReference<List<DeliveryZoneDto>>() {});
        } catch (Exception e) {
            log.warn("Error parsing branch delivery zones JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
