package com.resapori.e_commerce.northbound.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuAddOnRequest {
    @NotBlank(message = "Add-on name (EN) is required")
    private String nameEn;
    private String nameAr;
    @NotNull(message = "Add-on price is required")
    private BigDecimal price;
}
