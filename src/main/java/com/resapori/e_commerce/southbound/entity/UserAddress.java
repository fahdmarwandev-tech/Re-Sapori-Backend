package com.resapori.e_commerce.southbound.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_addresses")
public class UserAddress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Human-readable label, e.g. "Home" or "Work". */
    @Column(name = "label")
    private String label;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "floor")
    private String floor;

    @Column(name = "apartment")
    private String apartment;

    /** Latitude — nullable, reserved for future geo-features. */
    @Column(name = "lat", precision = 10, scale = 7)
    private BigDecimal lat;

    /** Longitude — nullable, reserved for future geo-features. */
    @Column(name = "lng", precision = 10, scale = 7)
    private BigDecimal lng;

    /** Marks this as the user's default delivery address. */
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
}
