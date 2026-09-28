package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.offer.OfferRequest;
import com.resapori.e_commerce.northbound.dto.offer.OfferResponse;
import com.resapori.e_commerce.southbound.entity.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OfferSlotMapper.class})
public interface OfferMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.nameEn", target = "categoryNameEn")
    @Mapping(source = "category.nameAr", target = "categoryNameAr")
    OfferResponse toResponse(Offer entity);

    List<OfferResponse> toResponseList(List<Offer> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Offer toEntity(OfferRequest request);
}
