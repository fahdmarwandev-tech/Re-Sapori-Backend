package com.resapori.e_commerce.northbound.dto.menu;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String subtitleEn;
    private String subtitleAr;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("isVisible")
    private Boolean isVisible;

    @JsonProperty("active")
    public Boolean getActive() {
        return isActive;
    }

    @JsonProperty("visible")
    public Boolean getVisible() {
        return isVisible;
    }
}
