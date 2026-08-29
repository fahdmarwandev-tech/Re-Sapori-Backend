package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryResponse;
import com.resapori.e_commerce.southbound.entity.MenuCategory;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MenuCategoryMapper {

    MenuCategory toEntity(MenuCategoryRequest request);

    MenuCategoryResponse toResponse(MenuCategory entity);

    List<MenuCategoryResponse> toResponseList(List<MenuCategory> entities);
}
