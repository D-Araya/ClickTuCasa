package com.clicktucasa.domain.exception;

/**
 * Thrown when the declared value of a house being raffled does not meet
 * the business rule enforced by the {@code HouseValue} value object
 * (must be a positive monetary amount).
 */
public class InvalidHouseValueException extends RuntimeException {
    public InvalidHouseValueException(String message) {
        super(message);
    }
}
