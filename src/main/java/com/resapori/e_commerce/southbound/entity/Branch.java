package com.resapori.e_commerce.southbound.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "branches")
public class Branch extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    /** Latitude — nullable, reserved for branch geo-location & delivery radius features. */
    @Column(name = "lat", precision = 10, scale = 7)
    private BigDecimal lat;

    /** Longitude — nullable, reserved for branch geo-location & delivery radius features. */
    @Column(name = "lng", precision = 10, scale = 7)
    private BigDecimal lng;

    /** JSON-encoded list of delivery zones (radius and polygon geofences) */
    @Column(name = "delivery_zones", columnDefinition = "TEXT")
    private String deliveryZones;
}
