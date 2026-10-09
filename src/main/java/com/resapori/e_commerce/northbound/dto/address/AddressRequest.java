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

    /** "DELIVERY" (default) or "CAR_PICKUP" */
    private String addressType;

    /** Human-readable label, e.g. "Home" or "Work". Optional. */
    private String label;

    private String street;

    private String building;

    private String city;

    private String district;

    private String floor;

    private String apartment;

    private String landmark;

    @com.fasterxml.jackson.annotation.JsonAlias({"phone", "contactPhone", "contact_phone", "phoneNumber", "phone_number"})
    private String phoneNumber;

    @com.fasterxml.jackson.annotation.JsonAlias({"car_plate", "plate"})
    private String carPlate;

    @com.fasterxml.jackson.annotation.JsonAlias({"car_details", "details", "model"})
    private String carDetails;

    /** Optional — for future geo/map features. */
    private BigDecimal lat;

    /** Optional — for future geo/map features. */
    private BigDecimal lng;

    /** When true, this address becomes the user's default delivery address. */
    @com.fasterxml.jackson.annotation.JsonProperty("isDefault")
    private boolean isDefault;
}
