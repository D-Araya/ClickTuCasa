package com.clicktucasa.infrastructure.payment;

import com.clicktucasa.domain.port.PaymentGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Simulated adapter for the {@link PaymentGateway} port. No real payment
 * processor (Stripe, MercadoPago, Webpay, etc.) is wired up yet — every
 * payment is accepted as long as the amount is a valid positive value —
 * so {@link com.clicktucasa.application.usecase.PurchaseTicketUseCase}
 * can be exercised end to end through the REST API. Swapping this for a
 * real gateway later only requires a new class implementing
 * {@link PaymentGateway}; no use case or controller changes.
 */
@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public boolean processPayment(String userId, BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
