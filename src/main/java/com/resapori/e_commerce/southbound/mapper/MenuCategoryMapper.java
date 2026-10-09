package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryResponse;
import com.resapori.e_commerce.southbound.entity.MenuCategory;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MenuCategoryMapper {

    @Mapping(target = "visible", source = "isVisible")
    MenuCategory toEntity(MenuCategoryRequest request);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "isVisible", source = "visible")
    @Mapping(target = "isActive", source = "active")
    MenuCategoryResponse toResponse(MenuCategory entity);

    List<MenuCategoryResponse> toResponseList(List<MenuCategory> entities);
}
