package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.menu.MenuAddOnRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuAddOnResponse;
import com.resapori.e_commerce.southbound.entity.MenuAddOn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuAddOnMapper {

    @Mapping(target = "menuItem", ignore = true)
    MenuAddOn toEntity(MenuAddOnRequest request);

    @Mapping(source = "active", target = "active")
    MenuAddOnResponse toResponse(MenuAddOn entity);

    List<MenuAddOnResponse> toResponseList(List<MenuAddOn> entities);
}
