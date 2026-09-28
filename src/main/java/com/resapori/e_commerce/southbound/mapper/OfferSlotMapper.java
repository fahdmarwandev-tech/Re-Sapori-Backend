package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.offer.OfferSlotRequest;
import com.resapori.e_commerce.northbound.dto.offer.OfferSlotResponse;
import com.resapori.e_commerce.southbound.entity.OfferSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MenuItemMapper.class})
public interface OfferSlotMapper {

    @Mapping(source = "menuItem", target = "fixedItem")
    @Mapping(source = "category.id", target = "eligibleCategoryId")
    @Mapping(source = "category.nameEn", target = "eligibleCategoryNameEn")
    @Mapping(source = "category.nameAr", target = "eligibleCategoryNameAr")
    @Mapping(source = "free", target = "isFree")
    OfferSlotResponse toResponse(OfferSlot entity);

    List<OfferSlotResponse> toResponseList(List<OfferSlot> entities);

    @Mapping(source = "isFree", target = "free")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "offer", ignore = true)
    @Mapping(target = "menuItem", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "eligibleItems", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    OfferSlot toEntity(OfferSlotRequest request);
}
