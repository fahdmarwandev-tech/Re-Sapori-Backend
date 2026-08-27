package com.resapori.e_commerce.southbound.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.resapori.e_commerce.southbound.enums.PaymentMethod;
import com.resapori.e_commerce.southbound.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Stores one payment attempt per Paymob transaction.
 * An order may have multiple Payment records (e.g., one failed attempt followed by a successful one).
 * The raw Paymob callback payload is persisted for debugging and reconciliation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Paymob's own order identifier returned from the intention/order creation step
    @Column(name = "paymob_order_id")
    private String paymobOrderId;

    // Paymob's transaction ID returned after the customer completes checkout
    @Column(name = "paymob_transaction_id", unique = true)
    private String paymobTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    // Amount in the smallest currency unit (piasters for EGP) as required by Paymob
    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "EGP";

    // Raw JSON body from Paymob's webhook/callback — stored for auditing and debugging
    @Column(name = "paymob_callback_raw", columnDefinition = "TEXT")
    private String paymobCallbackRaw;

}
