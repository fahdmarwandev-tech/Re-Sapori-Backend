package com.resapori.e_commerce.southbound.enums;

public enum PaymentMethod {
    CASH_ON_DELIVERY, // Paid in cash upon delivery / pickup
    CARD,             // Credit or debit card via Paymob
    WALLET,           // Mobile wallet via Paymob (e.g., Vodafone Cash, Fawry)
    KIOSK             // Paymob cash collection via Aman / Masary kiosk
}
