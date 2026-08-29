package com.resapori.e_commerce.northbound.dto.menu;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuCategoryResponse {
    private UUID id;
    private String nameEn;
    private String nameAr;
    private Integer displayOrder;
    private boolean isActive;
}
