package com.resapori.e_commerce.southbound.enums;

public enum PaymentStatus {
    PENDING,   // Payment intention created, awaiting user action
    SUCCESS,   // Paymob confirmed successful charge
    FAILED,    // Paymob confirmed charge failure
    REFUNDED,  // Payment was refunded to the customer
    VOIDED     // Payment was voided before capture
}
