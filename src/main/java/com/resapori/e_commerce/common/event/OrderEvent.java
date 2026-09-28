package com.resapori.e_commerce.common.event;

import com.resapori.e_commerce.northbound.dto.order.OrderResponse;

import com.resapori.e_commerce.southbound.enums.OrderStatus;

/**
 * Spring application event published after an order is created or its status changes.
 * Listeners must use {@code @TransactionalEventListener(phase = AFTER_COMMIT)} to avoid
 * broadcasting events for orders whose DB transaction subsequently rolled back.
 */
public record OrderEvent(OrderResponse order, EventType eventType, OrderStatus previousStatus) {

    public OrderEvent(OrderResponse order, EventType eventType) {
        this(order, eventType, null);
    }

    public enum EventType {
        NEW_ORDER,
        ORDER_UPDATED
    }
}
