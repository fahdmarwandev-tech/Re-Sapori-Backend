package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.address.AddressRequest;
import com.resapori.e_commerce.northbound.dto.address.AddressResponse;
import com.resapori.e_commerce.service.impl.UserAddressServiceImpl;
import com.resapori.e_commerce.southbound.entity.User;
import com.resapori.e_commerce.southbound.entity.UserAddress;
import com.resapori.e_commerce.southbound.repository.IUserAddressRepository;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceImplTest {

    @Mock
    private IUserAddressRepository addressRepository;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private UserAddressServiceImpl addressService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("client@resapori.com");
    }

    @Test
    @DisplayName("addAddress: Saves delivery address with structured building, landmark, and phone")
    void addAddress_DeliveryAddress_WithStructuredFields() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(addressRepository.save(any(UserAddress.class))).thenAnswer(i -> {
            UserAddress a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AddressRequest request = AddressRequest.builder()
                .addressType("DELIVERY")
                .label("Home")
                .street("Road 9")
                .building("Building 22")
                .floor("4")
                .apartment("15")
                .district("Maadi")
                .city("Cairo")
                .landmark("Next to Metro")
                .phoneNumber("01099887766")
                .lat(new BigDecimal("29.9600000"))
                .lng(new BigDecimal("31.2600000"))
                .isDefault(true)
                .build();

        AddressResponse response = addressService.addAddress(userId, request);

        assertNotNull(response);
        assertEquals("DELIVERY", response.getAddressType());
        assertEquals("Building 22", response.getBuilding());
        assertEquals("Next to Metro", response.getLandmark());
        assertEquals("01099887766", response.getPhoneNumber());

        ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
        verify(addressRepository).save(captor.capture());
        UserAddress saved = captor.getValue();
        assertEquals("DELIVERY", saved.getAddressType());
        assertEquals("Building 22", saved.getBuilding());
        assertEquals("Next to Metro", saved.getLandmark());
        assertEquals("01099887766", saved.getPhoneNumber());
        assertTrue(saved.isDefault());
    }

    @Test
    @DisplayName("addAddress: Saves car pickup vehicle with carPlate and carDetails")
    void addAddress_CarPickup_WithVehicleFields() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(addressRepository.save(any(UserAddress.class))).thenAnswer(i -> {
            UserAddress a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AddressRequest request = AddressRequest.builder()
                .addressType("CAR_PICKUP")
                .label("My Car")
                .carPlate("ق ر س 5678")
                .carDetails("Chery Tiggo 7 Black")
                .phoneNumber("01234567890")
                .isDefault(false)
                .build();

        AddressResponse response = addressService.addAddress(userId, request);

        assertNotNull(response);
        assertEquals("CAR_PICKUP", response.getAddressType());
        assertEquals("ق ر س 5678", response.getCarPlate());
        assertEquals("Chery Tiggo 7 Black", response.getCarDetails());
        assertEquals("01234567890", response.getPhoneNumber());

        ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
        verify(addressRepository).save(captor.capture());
        UserAddress saved = captor.getValue();
        assertEquals("CAR_PICKUP", saved.getAddressType());
        assertEquals("ق ر س 5678", saved.getCarPlate());
        assertEquals("Chery Tiggo 7 Black", saved.getCarDetails());
    }

    @Test
    @DisplayName("getAddresses: Returns both delivery addresses and car pickup profiles")
    void getAddresses_ReturnsMixedAddresses() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserAddress deliveryAddr = new UserAddress();
        deliveryAddr.setId(UUID.randomUUID());
        deliveryAddr.setUser(testUser);
        deliveryAddr.setAddressType("DELIVERY");
        deliveryAddr.setLabel("Home");
        deliveryAddr.setStreet("Street 10");
        deliveryAddr.setBuilding("Tower A");

        UserAddress carAddr = new UserAddress();
        carAddr.setId(UUID.randomUUID());
        carAddr.setUser(testUser);
        carAddr.setAddressType("CAR_PICKUP");
        carAddr.setLabel("Car");
        carAddr.setCarPlate("ABC 123");
        carAddr.setCarDetails("Red Kia Cerato");

        when(addressRepository.findByUserIdAndIsActiveTrue(userId)).thenReturn(List.of(deliveryAddr, carAddr));

        List<AddressResponse> responses = addressService.getAddresses(userId);

        assertEquals(2, responses.size());
        assertEquals("DELIVERY", responses.get(0).getAddressType());
        assertEquals("Tower A", responses.get(0).getBuilding());
        assertEquals("CAR_PICKUP", responses.get(1).getAddressType());
        assertEquals("ABC 123", responses.get(1).getCarPlate());
        assertEquals("Red Kia Cerato", responses.get(1).getCarDetails());
    }
}
