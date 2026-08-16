package com.clicktucasa.domain.valueobject;

import com.clicktucasa.domain.exception.InvalidHouseValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HouseValueTest {

    @Test
    @DisplayName("Should create HouseValue successfully when amount is positive")
    void shouldCreateHouseValueSuccessfully() {
        HouseValue value = new HouseValue(new BigDecimal("500000.00"));

        assertEquals(new BigDecimal("500000.00"), value.amount());
    }

    @Test
    @DisplayName("Should throw InvalidHouseValueException when amount is null")
    void shouldThrowExceptionWhenAmountIsNull() {
        assertThrows(InvalidHouseValueException.class, () -> new HouseValue(null));
    }

    @Test
    @DisplayName("Should throw InvalidHouseValueException when amount is zero")
    void shouldThrowExceptionWhenAmountIsZero() {
        assertThrows(InvalidHouseValueException.class, () -> new HouseValue(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should throw InvalidHouseValueException when amount is negative")
    void shouldThrowExceptionWhenAmountIsNegative() {
        assertThrows(InvalidHouseValueException.class, () -> new HouseValue(new BigDecimal("-100.00")));
    }
}
