package com.clicktucasa.domain.valueobject;

import com.clicktucasa.domain.exception.InvalidHouseAddressException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HouseAddressTest {

    @Test
    @DisplayName("Should create HouseAddress and trim surrounding whitespace")
    void shouldCreateHouseAddressSuccessfully() {
        HouseAddress address = new HouseAddress("  123 Ocean Drive  ");

        assertEquals("123 Ocean Drive", address.value());
    }

    @Test
    @DisplayName("Should throw InvalidHouseAddressException when value is null")
    void shouldThrowExceptionWhenValueIsNull() {
        assertThrows(InvalidHouseAddressException.class, () -> new HouseAddress(null));
    }

    @Test
    @DisplayName("Should throw InvalidHouseAddressException when value is blank")
    void shouldThrowExceptionWhenValueIsBlank() {
        assertThrows(InvalidHouseAddressException.class, () -> new HouseAddress("   "));
    }
}
