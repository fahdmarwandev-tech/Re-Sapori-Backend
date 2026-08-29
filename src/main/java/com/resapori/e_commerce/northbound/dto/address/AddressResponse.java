package com.resapori.e_commerce.northbound.dto.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {

    private UUID id;

    private String label;

    private String street;

    private String city;

    private String district;

    private String floor;

    private String apartment;

    private BigDecimal lat;

    private BigDecimal lng;

    private boolean isDefault;

    private LocalDateTime createdAt;
}
