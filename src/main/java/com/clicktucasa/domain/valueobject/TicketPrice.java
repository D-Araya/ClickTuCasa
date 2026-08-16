package com.clicktucasa.domain.valueobject;

import com.clicktucasa.domain.exception.InvalidTicketPriceException;

import java.math.BigDecimal;

/**
 * Value Object that protects the business rule of a raffle ticket: its
 * price must always be a positive monetary amount. Replaces a loose
 * {@code BigDecimal} attribute with a self-validating, immutable type.
 */
public record TicketPrice(BigDecimal amount) {

    // Compact constructor: defensive auto-validation on every instantiation.
    public TicketPrice {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTicketPriceException("Ticket price must be greater than zero");
        }
    }
}
