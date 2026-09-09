package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.menu.MenuItemRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuItemResponse;
import com.resapori.e_commerce.southbound.entity.MenuItem;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class MenuItemMapper {

    @Autowired
    protected MenuAddOnMapper menuAddOnMapper;

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "addOns", ignore = true)
    public abstract MenuItem toEntity(MenuItemRequest request);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.nameEn", target = "categoryNameEn")
    @Mapping(source = "available", target = "available")
    @Mapping(source = "active", target = "active")
    @Mapping(target = "addOns", expression = "java(menuAddOnMapper.toResponseList(entity.getAddOns()))")
    public abstract MenuItemResponse toResponse(MenuItem entity);

    public abstract List<MenuItemResponse> toResponseList(List<MenuItem> entities);
}
