package com.clicktucasa.domain.exception;

/**
 * Thrown by use cases when a raffle id passed to {@code RaffleRepository}
 * does not correspond to any stored raffle.
 */
public class RaffleNotFoundException extends RuntimeException {
    public RaffleNotFoundException(String message) {
        super(message);
    }
}
