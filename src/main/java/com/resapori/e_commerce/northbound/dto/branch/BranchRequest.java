package com.resapori.e_commerce.northbound.dto.branch;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.resapori.e_commerce.northbound.dto.delivery.DeliveryZoneDto;
import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BranchRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String address;
    private String phoneNumber;

    @JsonProperty("isActive")
    @JsonAlias({"active", "isActive"})
    private Boolean isActive;

    private BigDecimal lat;
    private BigDecimal lng;
    private List<DeliveryZoneDto> deliveryZones;

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setActive(Boolean active) {
        this.isActive = active;
    }
}
