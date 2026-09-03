package com.resapori.e_commerce.northbound.dto.menu;

import com.resapori.e_commerce.common.validation.CloudinaryUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemRequest {
    @NotNull(message = "Category ID is required")
    private UUID categoryId;
    @NotBlank(message = "Name (EN) is required")
    private String nameEn;
    private String nameAr;
    private String descriptionEn;
    private String descriptionAr;
    @NotNull(message = "Current price is required")
    private BigDecimal currentPrice;
    @CloudinaryUrl
    private String imageUrl;
    @Builder.Default
    private Boolean available = true;
    private Integer stock;
}
