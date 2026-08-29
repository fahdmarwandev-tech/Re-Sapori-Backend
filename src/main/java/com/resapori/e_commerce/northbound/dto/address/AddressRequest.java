package com.resapori.e_commerce.northbound.dto.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {

    /** Human-readable label, e.g. "Home" or "Work". Optional. */
    private String label;

    private String street;

    private String city;

    private String district;

    private String floor;

    private String apartment;

    /** Optional — for future geo/map features. */
    private BigDecimal lat;

    /** Optional — for future geo/map features. */
    private BigDecimal lng;

    /** When true, this address becomes the user's default delivery address. */
    private boolean isDefault;
}
