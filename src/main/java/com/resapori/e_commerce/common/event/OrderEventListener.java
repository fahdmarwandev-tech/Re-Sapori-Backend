package com.resapori.e_commerce.common.event;

import com.resapori.e_commerce.common.sse.SseEmitterRegistry;
import com.resapori.e_commerce.northbound.dto.order.OrderResponse;
import com.resapori.e_commerce.service.IEmailService;
import com.resapori.e_commerce.service.IEmailTemplateService;
import com.resapori.e_commerce.service.impl.EmailTemplateServiceImpl;
import com.resapori.e_commerce.southbound.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;

/**
 * Listens for {@link OrderEvent}s and fans them out to all connected SSE admin clients,
 * and dispatches customer email notifications for critical order status transitions.
 *
 * <p><strong>Why {@code @TransactionalEventListener(AFTER_COMMIT)}?</strong><br>
 * Using a plain {@code @EventListener} would broadcast the event even if the enclosing
 * database transaction rolls back, causing the admin panel to show phantom orders that
 * were never actually persisted. {@code AFTER_COMMIT} ensures the event is only delivered
 * once the DB write is fully durable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final SseEmitterRegistry registry;
    private final IEmailService emailService;
    private final IEmailTemplateService emailTemplateService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderEvent(OrderEvent event) {
        String sseEventName = switch (event.eventType()) {
            case NEW_ORDER -> "new_order";
            case ORDER_UPDATED -> "order_updated";
        };
        log.info("Broadcasting SSE event '{}' for order {} to {} client(s)",
                sseEventName, event.order().getId(), registry.connectedCount());
        registry.broadcast(sseEventName, event.order());

        dispatchEmailNotification(event);
    }

    private void dispatchEmailNotification(OrderEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                OrderResponse order = event.order();
                if (order == null) return;

                String customerEmail = order.getCustomerEmail();
                if (customerEmail == null || customerEmail.trim().isBlank()) {
                    log.debug("Skipping email notification for order {}: no customer email available", order.getId());
                    return;
                }

                OrderStatus currentStatus = order.getStatus();
                OrderStatus previousStatus = event.previousStatus();

                if (currentStatus == OrderStatus.PREPARING && previousStatus != OrderStatus.PREPARING) {
                    log.info("Sending order confirmed & preparing email for order {} to {}", order.getId(), customerEmail);
                    String orderNumber = EmailTemplateServiceImpl.formatOrderNumber(order.getId());
                    String subject = "Re Sapori - Order Confirmed & In The Kitchen! [" + orderNumber + "]";
                    String htmlBody = emailTemplateService.buildOrderConfirmedEmail(order);
                    emailService.sendHtmlEmail(customerEmail, subject, htmlBody);
                } else if (currentStatus == OrderStatus.CANCELLED && previousStatus != OrderStatus.CANCELLED) {
                    log.info("Sending order cancellation notice for order {} to {}", order.getId(), customerEmail);
                    String orderNumber = EmailTemplateServiceImpl.formatOrderNumber(order.getId());
                    String subject = "Re Sapori - Order Cancellation Notice [" + orderNumber + "]";
                    String htmlBody = emailTemplateService.buildOrderCancelledEmail(order);
                    emailService.sendHtmlEmail(customerEmail, subject, htmlBody);
                }
            } catch (Exception ex) {
                log.warn("Error processing email notification for order {}: {}",
                        event.order() != null ? event.order().getId() : "null", ex.getMessage());
            }
        });
    }
}
