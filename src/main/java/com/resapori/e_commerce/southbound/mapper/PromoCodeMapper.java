package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRequest;
import com.resapori.e_commerce.northbound.dto.promo.PromoCodeResponse;
import com.resapori.e_commerce.southbound.entity.PromoCode;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromoCodeMapper {

    @Mapping(target = "freeItem", ignore = true)
    @Mapping(target = "user", ignore = true)
    PromoCode toEntity(PromoCodeRequest request);

    @Mapping(source = "freeItem.id", target = "freeItemId")
    @Mapping(source = "user.id", target = "userId")
    PromoCodeResponse toResponse(PromoCode entity);

    List<PromoCodeResponse> toResponseList(List<PromoCode> entities);
}
