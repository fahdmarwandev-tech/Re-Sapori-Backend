package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.payment.PaymentResponse;
import com.resapori.e_commerce.southbound.entity.Payment;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    PaymentResponse toResponse(Payment entity);

    List<PaymentResponse> toResponseList(List<Payment> entities);
}
