package com.clicktucasa.domain.valueobject;

import com.clicktucasa.domain.exception.InvalidTicketPriceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TicketPriceTest {

    @Test
    @DisplayName("Should create TicketPrice successfully when amount is positive")
    void shouldCreateTicketPriceSuccessfully() {
        TicketPrice price = new TicketPrice(new BigDecimal("25.50"));

        assertEquals(new BigDecimal("25.50"), price.amount());
    }

    @Test
    @DisplayName("Should throw InvalidTicketPriceException when amount is null")
    void shouldThrowExceptionWhenAmountIsNull() {
        assertThrows(InvalidTicketPriceException.class, () -> new TicketPrice(null));
    }

    @Test
    @DisplayName("Should throw InvalidTicketPriceException when amount is zero")
    void shouldThrowExceptionWhenAmountIsZero() {
        assertThrows(InvalidTicketPriceException.class, () -> new TicketPrice(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should throw InvalidTicketPriceException when amount is negative")
    void shouldThrowExceptionWhenAmountIsNegative() {
        assertThrows(InvalidTicketPriceException.class, () -> new TicketPrice(new BigDecimal("-5.00")));
    }

    @Test
    @DisplayName("Should consider two TicketPrice instances with the same amount equal")
    void shouldBeEqualWhenAmountsMatch() {
        assertEquals(new TicketPrice(new BigDecimal("10.00")), new TicketPrice(new BigDecimal("10.00")));
    }
}
