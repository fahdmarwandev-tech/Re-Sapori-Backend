package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.order.OrderItemResponse;
import com.resapori.e_commerce.southbound.entity.OrderItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "menuItem.id", target = "menuItemId")
    @Mapping(source = "menuItem.nameEn", target = "nameEn")
    @Mapping(source = "menuItem.nameAr", target = "nameAr")
    @Mapping(source = "offer.id", target = "offerId")
    @Mapping(source = "offer.nameEn", target = "offerName")
    @Mapping(source = "free", target = "isFree")
    @Mapping(target = "lineTotal", ignore = true)
    OrderItemResponse toResponse(OrderItem entity);

    List<OrderItemResponse> toResponseList(List<OrderItem> entities);
}
