package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.order.OrderResponse;

public interface IEmailTemplateService {

    String buildOtpEmail(String otp, int expirationMinutes);

    String buildOrderConfirmedEmail(OrderResponse order);

    String buildOrderCancelledEmail(OrderResponse order);
}
