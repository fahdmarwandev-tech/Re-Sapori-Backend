package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.promo.PromoCodeRedemptionResponse;
import com.resapori.e_commerce.southbound.entity.PromoCodeRedemption;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromoCodeRedemptionMapper {

    @Mapping(source = "promoCode.id", target = "promoCodeId")
    @Mapping(source = "promoCode.code", target = "promoCode")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "order.id", target = "orderId")
    PromoCodeRedemptionResponse toResponse(PromoCodeRedemption entity);

    List<PromoCodeRedemptionResponse> toResponseList(List<PromoCodeRedemption> entities);
}
