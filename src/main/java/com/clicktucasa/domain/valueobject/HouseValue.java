package com.clicktucasa.domain.valueobject;

import com.clicktucasa.domain.exception.InvalidHouseValueException;

import java.math.BigDecimal;

/**
 * Value Object that protects the business rule of the house being raffled:
 * its declared value must always be a positive monetary amount.
 */
public record HouseValue(BigDecimal amount) {

    // Compact constructor: defensive auto-validation on every instantiation.
    public HouseValue {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidHouseValueException("House value must be greater than zero");
        }
    }
}
