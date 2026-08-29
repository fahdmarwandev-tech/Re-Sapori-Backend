package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.order.OrderResponse;
import com.resapori.e_commerce.southbound.entity.Order;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "branch.name", target = "branchName")
    @Mapping(target = "items", ignore = true)
    OrderResponse toResponse(Order entity);

    List<OrderResponse> toResponseList(List<Order> entities);
}
