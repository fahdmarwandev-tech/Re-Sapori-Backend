package com.resapori.e_commerce.northbound.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryZoneDto {

    /** Unique client/server ID for this zone */
    private String id;

    /** Human-readable zone name (e.g. "Sheikh Zayed", "October 1st District", "Compound Beverly Hills") */
    private String name;

    /** Zone geometry type: "RADIUS" (Circle) or "POLYGON" (Custom shape) */
    @Builder.Default
    private String type = "RADIUS";

    /** Center latitude for RADIUS zone */
    private BigDecimal centerLat;

    /** Center longitude for RADIUS zone */
    private BigDecimal centerLng;

    /** Radius in kilometers for RADIUS zone */
    private Double radiusKm;

    /**
     * Coordinates for POLYGON zone: list of points [[lat, lng], [lat, lng], ...]
     */
    private List<List<Double>> coordinates;

    /** Optional custom delivery fee override for this specific zone (in EGP) */
    private BigDecimal deliveryFee;

    /** Visual color hex code for map display (e.g. "#E53935", "#1E88E5", "#43A047") */
    private String color;

    /** Whether this zone is currently active */
    @Builder.Default
    private Boolean isActive = true;
}
