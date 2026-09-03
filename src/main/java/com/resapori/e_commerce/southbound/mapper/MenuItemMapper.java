package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.menu.MenuItemRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuItemResponse;
import com.resapori.e_commerce.southbound.entity.MenuItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "category", ignore = true)
    MenuItem toEntity(MenuItemRequest request);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.nameEn", target = "categoryNameEn")
    @Mapping(source = "available", target = "available")
    @Mapping(source = "active", target = "active")
    MenuItemResponse toResponse(MenuItem entity);

    List<MenuItemResponse> toResponseList(List<MenuItem> entities);
}
