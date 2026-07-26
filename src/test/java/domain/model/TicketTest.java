package domain.model;

import domain.exception.InvalidTicketPriceException;
import domain.exception.TicketNotAvailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    @DisplayName("Should create a ticket with default AVAILABLE status when valid parameters are provided")
    void shouldCreateTicketWithAvailableStatus() {
        // ARRANGE
        Long ticketNumber = 101L;
        BigDecimal price = new BigDecimal("25.00");

        // ACT
        Ticket ticket = new Ticket(ticketNumber, price);

        // ASSERT
        assertEquals(ticketNumber, ticket.getNumber());
        assertEquals(price, ticket.getPrice());
        assertEquals(TicketStatus.AVAILABLE, ticket.getStatus());
        assertNull(ticket.getOwnerId());
        assertNull(ticket.getReservedUntil());
    }

    @Test
    @DisplayName("Should create a ticket with specified status when constructor with status is called")
    void shouldCreateTicketWithSpecifiedStatus() {
        // ARRANGE
        Long ticketNumber = 102L;
        BigDecimal price = new BigDecimal("50.00");

        // ACT
        Ticket ticket = new Ticket(ticketNumber, price, TicketStatus.SOLD);

        // ASSERT
        assertEquals(TicketStatus.SOLD, ticket.getStatus());
    }

    @Test
    @DisplayName("Should default to AVAILABLE status if passed status is null in constructor")
    void shouldDefaultToAvailableStatusWhenPassedNullStatus() {
        // ARRANGE & ACT
        Ticket ticket = new Ticket(103L, new BigDecimal("10.00"), null);

        // ASSERT
        assertEquals(TicketStatus.AVAILABLE, ticket.getStatus());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when ticket number is null or zero/negative")
    void shouldThrowExceptionWhenTicketNumberIsInvalid() {
        // ARRANGE & ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> new Ticket(null, new BigDecimal("10.00")));
        assertThrows(IllegalArgumentException.class, () -> new Ticket(0L, new BigDecimal("10.00")));
        assertThrows(IllegalArgumentException.class, () -> new Ticket(-5L, new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("Should throw InvalidTicketPriceException when ticket price is null or zero/negative")
    void shouldThrowExceptionWhenTicketPriceIsInvalid() {
        // ARRANGE & ACT & ASSERT
        assertThrows(InvalidTicketPriceException.class, () -> new Ticket(100L, null));
        assertThrows(InvalidTicketPriceException.class, () -> new Ticket(100L, BigDecimal.ZERO));
        assertThrows(InvalidTicketPriceException.class, () -> new Ticket(100L, new BigDecimal("-10.00")));
    }

    @Test
    @DisplayName("Should successfully reserve ticket when it is AVAILABLE")
    void shouldReserveTicketSuccessfully() {
        // ARRANGE
        Ticket ticket = new Ticket(201L, new BigDecimal("15.00"));
        String userId = "user-123";
        LocalDateTime now = LocalDateTime.of(2026, 7, 26, 10, 0);

        // ACT
        ticket.reserve(userId, 10, now);

        // ASSERT
        assertEquals(TicketStatus.RESERVED, ticket.getStatus());
        assertEquals(userId, ticket.getOwnerId());
        assertEquals(now.plusMinutes(10), ticket.getReservedUntil());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when reserve is called with empty userId or non-positive duration")
    void shouldThrowExceptionWhenReserveParametersAreInvalid() {
        // ARRANGE
        Ticket ticket = new Ticket(202L, new BigDecimal("15.00"));
        LocalDateTime now = LocalDateTime.now();

        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> ticket.reserve(null, 10, now));
        assertThrows(IllegalArgumentException.class, () -> ticket.reserve("  ", 10, now));
        assertThrows(IllegalArgumentException.class, () -> ticket.reserve("user-1", 0, now));
        assertThrows(IllegalArgumentException.class, () -> ticket.reserve("user-1", -5, now));
    }

    @Test
    @DisplayName("Should throw TicketNotAvailableException when attempting to reserve a non-AVAILABLE ticket")
    void shouldThrowExceptionWhenReservingNonAvailableTicket() {
        // ARRANGE
        Ticket ticket = new Ticket(203L, new BigDecimal("15.00"), TicketStatus.SOLD);

        // ACT & ASSERT
        assertThrows(TicketNotAvailableException.class, () -> ticket.reserve("user-1", 10, LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should accurately identify if reservation is expired")
    void shouldCheckIfReservationIsExpired() {
        // ARRANGE
        Ticket ticket = new Ticket(204L, new BigDecimal("15.00"));
        LocalDateTime now = LocalDateTime.of(2026, 7, 26, 10, 0);
        ticket.reserve("user-1", 10, now);

        // ACT & ASSERT
        assertFalse(ticket.isReservationExpired(now.plusMinutes(5)));
        assertFalse(ticket.isReservationExpired(now.plusMinutes(10)));
        assertTrue(ticket.isReservationExpired(now.plusMinutes(11)));
    }

    @Test
    @DisplayName("Should return false for isReservationExpired if ticket is not RESERVED")
    void shouldReturnFalseForExpirationWhenNotReserved() {
        // ARRANGE
        Ticket ticket = new Ticket(205L, new BigDecimal("15.00"));

        // ACT & ASSERT
        assertFalse(ticket.isReservationExpired(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should return false for isReservationExpired when status is RESERVED but reservedUntil is null")
    void shouldReturnFalseForExpirationWhenReservedUntilIsNull() {
        // ARRANGE
        Ticket ticket = new Ticket(300L, new BigDecimal("15.00"), TicketStatus.RESERVED);

        // ACT & ASSERT
        assertFalse(ticket.isReservationExpired(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should release reservation when ticket is in RESERVED status")
    void shouldReleaseReservation() {
        // ARRANGE
        Ticket ticket = new Ticket(206L, new BigDecimal("15.00"));
        ticket.reserve("user-1", 10, LocalDateTime.now());

        // ACT
        ticket.releaseReservation();

        // ASSERT
        assertEquals(TicketStatus.AVAILABLE, ticket.getStatus());
        assertNull(ticket.getOwnerId());
        assertNull(ticket.getReservedUntil());
    }

    @Test
    @DisplayName("Should do nothing when releaseReservation is called on non-RESERVED ticket")
    void shouldDoNothingWhenReleasingNonReservedTicket() {
        // ARRANGE
        Ticket ticket = new Ticket(207L, new BigDecimal("15.00"), TicketStatus.SOLD);

        // ACT
        ticket.releaseReservation();

        // ASSERT
        assertEquals(TicketStatus.SOLD, ticket.getStatus());
    }

    @Test
    @DisplayName("Should assign ticket to owner and clear reservation when assignToOwner is called")
    void shouldAssignTicketToOwnerSuccessfully() {
        // ARRANGE
        Ticket ticket = new Ticket(208L, new BigDecimal("15.00"));
        String ownerId = "buyer-999";

        // ACT
        ticket.assignToOwner(ownerId);

        // ASSERT
        assertEquals(TicketStatus.SOLD, ticket.getStatus());
        assertEquals(ownerId, ticket.getOwnerId());
        assertNull(ticket.getReservedUntil());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when assignToOwner receives empty ownerId")
    void shouldThrowExceptionWhenAssignToOwnerWithEmptyId() {
        // ARRANGE
        Ticket ticket = new Ticket(209L, new BigDecimal("15.00"));

        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> ticket.assignToOwner(null));
        assertThrows(IllegalArgumentException.class, () -> ticket.assignToOwner(" "));
    }

    @Test
    @DisplayName("Should throw TicketNotAvailableException when attempting to assign a ticket that is already SOLD")
    void shouldThrowExceptionWhenAssigningAlreadySoldTicket() {
        // ARRANGE
        Ticket ticket = new Ticket(210L, new BigDecimal("15.00"));
        ticket.assignToOwner("buyer-1");

        // ACT & ASSERT
        assertThrows(TicketNotAvailableException.class, () -> ticket.assignToOwner("buyer-2"));
    }
}
