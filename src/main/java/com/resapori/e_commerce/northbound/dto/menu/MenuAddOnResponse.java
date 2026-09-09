package com.resapori.e_commerce.northbound.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuAddOnResponse {
    private UUID id;
    private String nameEn;
    private String nameAr;
    private BigDecimal price;
    private Boolean active;
}
