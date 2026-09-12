package com.resapori.e_commerce.common.event;

import com.resapori.e_commerce.common.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens for {@link OrderEvent}s and fans them out to all connected SSE admin clients.
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderEvent(OrderEvent event) {
        String sseEventName = switch (event.eventType()) {
            case NEW_ORDER -> "new_order";
            case ORDER_UPDATED -> "order_updated";
        };
        log.info("Broadcasting SSE event '{}' for order {} to {} client(s)",
                sseEventName, event.order().getId(), registry.connectedCount());
        registry.broadcast(sseEventName, event.order());
    }
}
