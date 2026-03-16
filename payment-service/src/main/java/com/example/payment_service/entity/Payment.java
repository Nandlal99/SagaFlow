package com.example.payment_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private String transactionId;       // From the gateway (Stripe/PayPal)
    private Double amount;
    private String paymentStatus;      // SUCCESS, FAILED, REFUNDED
    private String failureReason;     // e.g., "INSUFFICIENT_FUNDS"
}
