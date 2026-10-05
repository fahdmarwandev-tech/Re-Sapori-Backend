package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.delivery.CalculateDeliveryFeeRequest;
import com.resapori.e_commerce.northbound.dto.delivery.DeliveryFeeResponse;
import com.resapori.e_commerce.service.impl.DeliveryServiceImpl;
import com.resapori.e_commerce.southbound.entity.Branch;
import com.resapori.e_commerce.southbound.entity.UserAddress;
import com.resapori.e_commerce.southbound.repository.IBranchRepository;
import com.resapori.e_commerce.southbound.repository.IUserAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest {

    @Mock
    private IBranchRepository branchRepository;

    @Mock
    private IUserAddressRepository userAddressRepository;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private Branch octoberBranch;
    private Branch zamalekBranch;

    @BeforeEach
    void setUp() {
        octoberBranch = new Branch();
        octoberBranch.setId(UUID.randomUUID());
        octoberBranch.setName("6th of October Hub");
        octoberBranch.setActive(true);
        octoberBranch.setLat(BigDecimal.valueOf(29.996688));
        octoberBranch.setLng(BigDecimal.valueOf(30.983078));

        zamalekBranch = new Branch();
        zamalekBranch.setId(UUID.randomUUID());
        zamalekBranch.setName("Zamalek Hub");
        zamalekBranch.setActive(true);
        zamalekBranch.setLat(BigDecimal.valueOf(30.0609));
        zamalekBranch.setLng(BigDecimal.valueOf(31.2197));
    }

    @Test
    @DisplayName("calculateDeliveryFee: When distance <= 4.0 km, fee is flat 35.00 EGP")
    void calculateDeliveryFee_UnderOrEqual4Km_FlatFee() {
        assertEquals(new BigDecimal("35.00"), deliveryService.calculateDeliveryFee(new BigDecimal("0.50")));
        assertEquals(new BigDecimal("35.00"), deliveryService.calculateDeliveryFee(new BigDecimal("2.00")));
        assertEquals(new BigDecimal("35.00"), deliveryService.calculateDeliveryFee(new BigDecimal("4.00")));
        assertEquals(new BigDecimal("35.00"), deliveryService.calculateDeliveryFee((BigDecimal) null));
        assertEquals(new BigDecimal("35.00"), deliveryService.calculateDeliveryFee(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("calculateDeliveryFee: When distance > 4.0 km, fee is 35 + 7 * (d - 4) rounded to nearest integer")
    void calculateDeliveryFee_Over4Km_CalculatedFee() {
        // d = 5 km -> 35 + 7(1) = 42.00 EGP
        assertEquals(new BigDecimal("42.00"), deliveryService.calculateDeliveryFee(new BigDecimal("5.00")));

        // d = 6.8 km -> 35 + 7(2.8) = 35 + 19.6 = 54.60 -> rounded to nearest integer = 55.00 EGP
        assertEquals(new BigDecimal("55.00"), deliveryService.calculateDeliveryFee(new BigDecimal("6.80")));

        // d = 6.2 km -> 35 + 7(2.2) = 35 + 15.4 = 50.40 -> rounded to nearest integer = 50.00 EGP
        assertEquals(new BigDecimal("50.00"), deliveryService.calculateDeliveryFee(new BigDecimal("6.20")));

        // d = 10 km -> 35 + 7(6) = 35 + 42 = 77.00 EGP
        assertEquals(new BigDecimal("77.00"), deliveryService.calculateDeliveryFee(new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("calculateDistanceKm: Computes geodesic distance correctly via Haversine")
    void calculateDistanceKm_ComputesCorrectly() {
        // Distance between October branch and Zamalek branch (~27 km)
        BigDecimal distance = deliveryService.calculateDistanceKm(
                octoberBranch.getLat(), octoberBranch.getLng(),
                zamalekBranch.getLat(), zamalekBranch.getLng()
        );
        assertNotNull(distance);
        assertTrue(distance.compareTo(new BigDecimal("20.00")) > 0);
        assertTrue(distance.compareTo(new BigDecimal("30.00")) < 0);
    }

    @Test
    @DisplayName("calculateDeliveryFee: Resolves nearest active branch")
    void calculateDeliveryFee_ResolvesNearestBranch() {
        when(branchRepository.findByIsActiveTrue()).thenReturn(List.of(octoberBranch, zamalekBranch));

        // Location right next to October Branch
        CalculateDeliveryFeeRequest req = CalculateDeliveryFeeRequest.builder()
                .lat(BigDecimal.valueOf(29.9980))
                .lng(BigDecimal.valueOf(30.9840))
                .build();

        DeliveryFeeResponse res = deliveryService.calculateDeliveryFee(req);

        assertNotNull(res);
        assertEquals(octoberBranch.getId(), res.getBranchId());
        assertEquals("6th of October Hub", res.getBranchName());
        assertTrue(res.getDistanceKm().compareTo(new BigDecimal("1.00")) < 0);
        assertEquals(new BigDecimal("35.00"), res.getDeliveryFee());
    }

    @Test
    @DisplayName("calculateForAddress: Loads coordinates from UserAddress")
    void calculateForAddress_LoadsUserAddressCoordinates() {
        UUID addressId = UUID.randomUUID();
        UserAddress address = new UserAddress();
        address.setId(addressId);
        address.setLat(BigDecimal.valueOf(29.9980));
        address.setLng(BigDecimal.valueOf(30.9840));

        when(userAddressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(branchRepository.findByIsActiveTrue()).thenReturn(List.of(octoberBranch));

        DeliveryFeeResponse res = deliveryService.calculateForAddress(addressId, null);

        assertNotNull(res);
        assertEquals(new BigDecimal("35.00"), res.getDeliveryFee());
        assertEquals(octoberBranch.getId(), res.getBranchId());
    }

    @Test
    @DisplayName("calculateDeliveryFee: When branch has configured zones and customer is outside, returns isCovered=false and fee=0")
    void calculateDeliveryFee_OutsideConfiguredZones_ReturnsUncoveredWithZeroFee() {
        // Configure October branch with a strict 3km radius zone
        octoberBranch.setDeliveryZones("[{\"id\":\"z1\",\"name\":\"Local Zone\",\"type\":\"RADIUS\",\"radiusKm\":3.0,\"isActive\":true,\"centerLat\":29.996688,\"centerLng\":30.983078}]");
        when(branchRepository.findByIsActiveTrue()).thenReturn(List.of(octoberBranch));

        // Location 10 km away (far outside the 3km zone)
        CalculateDeliveryFeeRequest req = CalculateDeliveryFeeRequest.builder()
                .lat(BigDecimal.valueOf(30.0800))
                .lng(BigDecimal.valueOf(31.0500))
                .build();

        DeliveryFeeResponse res = deliveryService.calculateDeliveryFee(req);

        assertNotNull(res);
        assertFalse(res.isCovered());
        assertEquals(BigDecimal.ZERO, res.getDeliveryFee());
    }
}
