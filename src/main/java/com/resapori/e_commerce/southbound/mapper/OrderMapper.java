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
    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "customerName", expression = "java(formatCustomerName(entity))")
    @Mapping(source = "user.email", target = "customerEmail")
    @Mapping(source = "user.phoneNumber", target = "customerPhone")
    @Mapping(target = "items", ignore = true)
    OrderResponse toResponse(Order entity);

    default String formatCustomerName(Order entity) {
        if (entity.getUser() == null) return null;
        String first = entity.getUser().getFirstName() != null ? entity.getUser().getFirstName() : "";
        String last = entity.getUser().getLastName() != null ? entity.getUser().getLastName() : "";
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? null : fullName;
    }

    List<OrderResponse> toResponseList(List<Order> entities);
}
