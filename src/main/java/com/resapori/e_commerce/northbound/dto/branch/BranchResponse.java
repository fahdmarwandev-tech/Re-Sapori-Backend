package com.resapori.e_commerce.northbound.dto.branch;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.resapori.e_commerce.northbound.dto.delivery.DeliveryZoneDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BranchResponse {
    private UUID id;
    private String name;
    private String address;
    private String phoneNumber;

    @JsonProperty("isActive")
    @JsonAlias({"active", "isActive"})
    private boolean isActive;

    private BigDecimal lat;
    private BigDecimal lng;
    private List<DeliveryZoneDto> deliveryZones;

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    @JsonProperty("isActive")
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @JsonProperty("active")
    public boolean getActive() {
        return isActive;
    }
}
