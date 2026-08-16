package com.clicktucasa.domain.exception;

public class InvalidTicketPriceException extends RuntimeException {
    public InvalidTicketPriceException(String message) {
        super(message);
    }
}
