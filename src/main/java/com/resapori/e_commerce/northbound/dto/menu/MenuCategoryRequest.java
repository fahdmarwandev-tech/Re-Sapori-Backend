package com.resapori.e_commerce.northbound.dto.menu;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuCategoryRequest {
    @NotBlank(message = "Name (EN) is required")
    private String nameEn;
    private String nameAr;
    private Integer displayOrder;
    private String subtitleEn;
    private String subtitleAr;
    private Boolean isVisible;
    @Valid
    private List<MenuAddOnRequest> addOns;
}
