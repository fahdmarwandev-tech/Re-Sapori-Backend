package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryResponse;
import com.resapori.e_commerce.southbound.entity.MenuCategory;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class MenuCategoryMapper {

    @Autowired
    protected MenuAddOnMapper menuAddOnMapper;

    @Mapping(target = "visible", source = "isVisible")
    @Mapping(target = "addOns", ignore = true)
    public abstract MenuCategory toEntity(MenuCategoryRequest request);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "isVisible", source = "visible")
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "addOns", expression = "java(menuAddOnMapper.toResponseList(entity.getAddOns() == null ? java.util.Collections.emptyList() : entity.getAddOns().stream().filter(com.resapori.e_commerce.southbound.entity.BaseEntity::isActive).toList()))")
    public abstract MenuCategoryResponse toResponse(MenuCategory entity);

    public abstract List<MenuCategoryResponse> toResponseList(List<MenuCategory> entities);
}
