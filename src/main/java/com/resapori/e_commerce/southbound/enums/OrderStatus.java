package com.resapori.e_commerce.southbound.enums;

public enum OrderStatus {
    PENDING,         // Order created, awaiting payment
    PAYMENT_PENDING, // Paymob intention created, user redirected to checkout
    PAID,            // Paymob confirmed successful payment
    PAYMENT_FAILED,  // Paymob confirmed payment failure
    PREPARING,       // Kitchen is preparing the order
    READY,           // Order ready for pickup / out for delivery
    DELIVERED,       // Order delivered to customer
    CANCELLED        // Order cancelled
}
