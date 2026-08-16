package com.clicktucasa.domain.exception;

/**
 * Thrown when a house address does not meet the business rule enforced by
 * the {@code HouseAddress} value object (must be a non-blank value).
 */
public class InvalidHouseAddressException extends RuntimeException {
    public InvalidHouseAddressException(String message) {
        super(message);
    }
}
