package com.clicktucasa.domain.valueobject;

import com.clicktucasa.domain.exception.InvalidHouseAddressException;

/**
 * Value Object that protects the business rule of the house being raffled:
 * its physical address must always be present and non-blank.
 */
public record HouseAddress(String value) {

    // Compact constructor: defensive auto-validation on every instantiation.
    public HouseAddress {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidHouseAddressException("House address cannot be empty");
        }
        value = value.trim();
    }
}
