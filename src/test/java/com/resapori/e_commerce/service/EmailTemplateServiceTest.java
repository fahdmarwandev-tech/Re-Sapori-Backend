package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.order.OrderItemResponse;
import com.resapori.e_commerce.northbound.dto.order.OrderResponse;
import com.resapori.e_commerce.service.impl.EmailTemplateServiceImpl;
import com.resapori.e_commerce.southbound.enums.ItemSize;
import com.resapori.e_commerce.southbound.enums.OrderStatus;
import com.resapori.e_commerce.southbound.enums.OrderType;
import com.resapori.e_commerce.southbound.enums.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmailTemplateServiceTest {

    private final EmailTemplateServiceImpl templateService = new EmailTemplateServiceImpl();

    @Test
    @DisplayName("buildOtpEmail: Generates beautiful HTML containing OTP and expiration")
    void buildOtpEmail_generatesHtmlWithOtp() {
        String html = templateService.buildOtpEmail("984123", 5);

        assertNotNull(html);
        assertTrue(html.contains("984123"), "Should contain the OTP code");
        assertTrue(html.contains("5 minutes"), "Should specify expiration minutes");
        assertTrue(html.contains("RE SAPORI"), "Should include brand header");
        assertTrue(html.contains("Fine Dining &amp; Artisanal Pizzeria"));
    }

    @Test
    @DisplayName("buildOrderConfirmedEmail: Formats order items, customer, delivery address, and total")
    void buildOrderConfirmedEmail_containsCompleteOrderInfo() {
        UUID orderId = UUID.fromString("eca54611-1234-4567-8901-123456789abc");
        OrderResponse order = OrderResponse.builder()
                .id(orderId)
                .status(OrderStatus.PREPARING)
                .orderType(OrderType.DELIVERY)
                .deliveryAddress("Share3 October, Awal, Floor: 0, Apt: 11")
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .customerName("Abdelrahman")
                .customerEmail("abdo@example.com")
                .currency("EGP")
                .totalAmount(BigDecimal.valueOf(650.00))
                .createdAt(LocalDateTime.of(2026, 9, 28, 20, 15))
                .items(List.of(
                        OrderItemResponse.builder()
                                .nameEn("Creamy Salmon Pasta")
                                .quantity(1)
                                .size(ItemSize.REGULAR)
                                .unitPriceAtPurchase(BigDecimal.valueOf(650.00))
                                .lineTotal(BigDecimal.valueOf(650.00))
                                .isFree(false)
                                .build(),
                        OrderItemResponse.builder()
                                .nameEn("Tiramisu")
                                .quantity(1)
                                .size(ItemSize.REGULAR)
                                .unitPriceAtPurchase(BigDecimal.ZERO)
                                .lineTotal(BigDecimal.ZERO)
                                .isFree(true)
                                .build()
                ))
                .build();

        String html = templateService.buildOrderConfirmedEmail(order);

        assertNotNull(html);
        assertTrue(html.contains("#RS-ECA546"), "Should contain #RS- order reference");
        assertTrue(html.contains("Abdelrahman"), "Should greet customer");
        assertTrue(html.contains("Creamy Salmon Pasta"), "Should include dish name");
        assertTrue(html.contains("Tiramisu"), "Should include second dish name");
        assertTrue(html.contains("Promo Gift"), "Should show promo gift tag for free item");
        assertTrue(html.contains("Share3 October"), "Should show delivery address");
        assertTrue(html.contains("650.00"), "Should show total amount");
        assertTrue(html.contains("Confirmed &amp; Being Prepared"), "Should show confirmed badge");
    }

    @Test
    @DisplayName("buildOrderConfirmedEmail: Handles null/empty order safely without throwing")
    void buildOrderConfirmedEmail_handlesNullsSafely() {
        assertDoesNotThrow(() -> {
            String html = templateService.buildOrderConfirmedEmail(new OrderResponse());
            assertNotNull(html);
            assertTrue(html.contains("Valued Guest"));
            assertTrue(html.contains("#RS-000000"));
        });
    }

    @Test
    @DisplayName("buildOrderCancelledEmail: Contains cancellation message, order number, and refund guidance")
    void buildOrderCancelledEmail_containsCancellationNotice() {
        UUID orderId = UUID.fromString("717edc22-9999-4567-8901-abcdefabcdef");
        OrderResponse order = OrderResponse.builder()
                .id(orderId)
                .status(OrderStatus.CANCELLED)
                .customerName("Ahmed Khafagy")
                .customerEmail("ahmed@example.com")
                .totalAmount(BigDecimal.valueOf(350.00))
                .currency("EGP")
                .build();

        String html = templateService.buildOrderCancelledEmail(order);

        assertNotNull(html);
        assertTrue(html.contains("#RS-717EDC"), "Should contain formatted order number");
        assertTrue(html.contains("Ahmed Khafagy"));
        assertTrue(html.contains("Order Cancelled"));
        assertTrue(html.contains("350.00 EGP"));
        assertTrue(html.contains("refund"));
    }
}
