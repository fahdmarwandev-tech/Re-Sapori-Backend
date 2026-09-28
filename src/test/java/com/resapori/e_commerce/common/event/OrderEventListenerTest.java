package com.resapori.e_commerce.common.event;

import com.resapori.e_commerce.common.sse.SseEmitterRegistry;
import com.resapori.e_commerce.northbound.dto.order.OrderResponse;
import com.resapori.e_commerce.service.IEmailService;
import com.resapori.e_commerce.service.IEmailTemplateService;
import com.resapori.e_commerce.southbound.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock
    private SseEmitterRegistry registry;

    @Mock
    private IEmailService emailService;

    @Mock
    private IEmailTemplateService emailTemplateService;

    @InjectMocks
    private OrderEventListener listener;

    @Test
    @DisplayName("Should send confirmation email when order transitions to PREPARING")
    void handleOrderEvent_SendsEmailOnPreparing() {
        UUID orderId = UUID.randomUUID();
        OrderResponse order = OrderResponse.builder()
                .id(orderId)
                .status(OrderStatus.PREPARING)
                .customerEmail("customer@example.com")
                .customerName("Tamer")
                .build();

        when(emailTemplateService.buildOrderConfirmedEmail(order)).thenReturn("<html>Confirmed</html>");

        OrderEvent event = new OrderEvent(order, OrderEvent.EventType.ORDER_UPDATED, OrderStatus.PENDING);
        listener.handleOrderEvent(event);

        verify(registry).broadcast("order_updated", order);
        verify(emailService, timeout(2000)).sendHtmlEmail(
                eq("customer@example.com"),
                contains("Order Confirmed & In The Kitchen!"),
                eq("<html>Confirmed</html>")
        );
    }

    @Test
    @DisplayName("Should send cancellation notice when order transitions to CANCELLED")
    void handleOrderEvent_SendsEmailOnCancelled() {
        UUID orderId = UUID.randomUUID();
        OrderResponse order = OrderResponse.builder()
                .id(orderId)
                .status(OrderStatus.CANCELLED)
                .customerEmail("customer@example.com")
                .customerName("Tamer")
                .build();

        when(emailTemplateService.buildOrderCancelledEmail(order)).thenReturn("<html>Cancelled</html>");

        OrderEvent event = new OrderEvent(order, OrderEvent.EventType.ORDER_UPDATED, OrderStatus.PREPARING);
        listener.handleOrderEvent(event);

        verify(registry).broadcast("order_updated", order);
        verify(emailService, timeout(2000)).sendHtmlEmail(
                eq("customer@example.com"),
                contains("Order Cancellation Notice"),
                eq("<html>Cancelled</html>")
        );
    }

    @Test
    @DisplayName("Should not send email when status has not changed")
    void handleOrderEvent_DoesNotSendWhenStatusUnchanged() {
        UUID orderId = UUID.randomUUID();
        OrderResponse order = OrderResponse.builder()
                .id(orderId)
                .status(OrderStatus.PREPARING)
                .customerEmail("customer@example.com")
                .build();

        OrderEvent event = new OrderEvent(order, OrderEvent.EventType.ORDER_UPDATED, OrderStatus.PREPARING);
        listener.handleOrderEvent(event);

        verify(registry).broadcast("order_updated", order);
        verify(emailService, after(200).never()).sendHtmlEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should not send email when customer email is null or blank")
    void handleOrderEvent_DoesNotSendWhenCustomerEmailMissing() {
        UUID orderId = UUID.randomUUID();
        OrderResponse order = OrderResponse.builder()
                .id(orderId)
                .status(OrderStatus.PREPARING)
                .customerEmail(null)
                .build();

        OrderEvent event = new OrderEvent(order, OrderEvent.EventType.ORDER_UPDATED, OrderStatus.PENDING);
        listener.handleOrderEvent(event);

        verify(registry).broadcast("order_updated", order);
        verify(emailService, after(200).never()).sendHtmlEmail(any(), any(), any());
    }
}
