package domain.model;

import domain.exception.InvalidRaffleOperationException;
import domain.exception.TicketNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RaffleTest {

    private Ticket ticket1;
    private Ticket ticket2;
    private Ticket ticket3;
    private List<Ticket> ticketList;

    @BeforeEach
    void setUp() {
        ticket1 = new Ticket(1L, new BigDecimal("50.00"));
        ticket2 = new Ticket(2L, new BigDecimal("50.00"));
        ticket3 = new Ticket(3L, new BigDecimal("50.00"));
        ticketList = new ArrayList<>(List.of(ticket1, ticket2, ticket3));
    }

    @Test
    @DisplayName("Should create Raffle with ACTIVE status by default when valid parameters are provided")
    void shouldCreateRaffleSuccessfully() {
        // ARRANGE & ACT
        Raffle raffle = new Raffle("raf-001", "Luxury Villa Raffle", "123 Ocean Drive", new BigDecimal("500000.00"), 2, ticketList);

        // ASSERT
        assertEquals("raf-001", raffle.getId());
        assertEquals("Luxury Villa Raffle", raffle.getTitle());
        assertEquals("123 Ocean Drive", raffle.getHouseAddress());
        assertEquals(new BigDecimal("500000.00"), raffle.getHouseValue());
        assertEquals(2, raffle.getMinTicketsToDraw());
        assertEquals(3, raffle.getTickets().size());
        assertEquals(RaffleStatus.ACTIVE, raffle.getStatus());
        assertNull(raffle.getWinnerTicketNumber());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when Raffle constructor arguments are invalid")
    void shouldThrowExceptionWhenConstructorArgsAreInvalid() {
        // ARRANGE & ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> new Raffle(null, "Title", "Address", new BigDecimal("100"), 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle(" ", "Title", "Address", new BigDecimal("100"), 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", null, "Address", new BigDecimal("100"), 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "  ", "Address", new BigDecimal("100"), 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", null, new BigDecimal("100"), 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", "  ", new BigDecimal("100"), 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", "Address", null, 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", "Address", BigDecimal.ZERO, 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", "Address", new BigDecimal("100"), 0, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", "Address", new BigDecimal("100"), 1, null));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", "Address", new BigDecimal("100"), 1, List.of()));
    }

    @Test
    @DisplayName("Should find ticket by number successfully")
    void shouldFindTicketByNumber() {
        // ARRANGE
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 1, ticketList);

        // ACT
        Ticket found = raffle.findTicketByNumber(2L);

        // ASSERT
        assertNotNull(found);
        assertEquals(2L, found.getNumber());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when findTicketByNumber receives null ticket number")
    void shouldThrowExceptionWhenFindTicketByNumberWithNull() {
        // ARRANGE
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 1, ticketList);

        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> raffle.findTicketByNumber(null));
    }

    @Test
    @DisplayName("Should throw TicketNotFoundException when ticket number does not exist in raffle")
    void shouldThrowExceptionWhenTicketNotFound() {
        // ARRANGE
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 1, ticketList);

        // ACT & ASSERT
        assertThrows(TicketNotFoundException.class, () -> raffle.findTicketByNumber(999L));
    }

    @Test
    @DisplayName("Should correctly categorize available, reserved, and sold tickets")
    void shouldCategorizeTicketsByStatus() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        ticket2.reserve("user-2", 10, java.time.LocalDateTime.now());
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 1, ticketList);

        // ACT & ASSERT
        assertEquals(1, raffle.getAvailableTickets().size());
        assertEquals(3L, raffle.getAvailableTickets().get(0).getNumber());

        assertEquals(1, raffle.getReservedTickets().size());
        assertEquals(2L, raffle.getReservedTickets().get(0).getNumber());

        assertEquals(1, raffle.getSoldTickets().size());
        assertEquals(1L, raffle.getSoldTickets().get(0).getNumber());
    }

    @Test
    @DisplayName("Should evaluate canBeDrawn correctly based on status and sold tickets threshold")
    void shouldEvaluateCanBeDrawn() {
        // ARRANGE
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 2, ticketList);

        // ACT & ASSERT (0 sold < 2 min)
        assertFalse(raffle.canBeDrawn());

        // ACT & ASSERT (1 sold < 2 min)
        ticket1.assignToOwner("user-1");
        assertFalse(raffle.canBeDrawn());

        // ACT & ASSERT (2 sold >= 2 min)
        ticket2.assignToOwner("user-2");
        assertTrue(raffle.canBeDrawn());
    }

    @Test
    @DisplayName("Should return false for canBeDrawn when raffle is CANCELLED even if sold tickets threshold is met")
    void shouldReturnFalseForCanBeDrawnWhenCancelled() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        ticket2.assignToOwner("user-2");
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 2, ticketList);
        raffle.cancel();

        // ACT & ASSERT
        assertFalse(raffle.canBeDrawn());
    }

    @Test
    @DisplayName("Should mark raffle as DRAWN and record winner ticket when conditions are met")
    void shouldMarkAsDrawnSuccessfully() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        ticket2.assignToOwner("user-2");
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 2, ticketList);

        // ACT
        raffle.markAsDrawn(1L);

        // ASSERT
        assertEquals(RaffleStatus.DRAWN, raffle.getStatus());
        assertEquals(1L, raffle.getWinnerTicketNumber());
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when markAsDrawn is called on non-ACTIVE raffle")
    void shouldThrowExceptionWhenMarkingDrawnOnNonActiveRaffle() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 1, ticketList);
        raffle.cancel();

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, () -> raffle.markAsDrawn(1L));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when minimum tickets for draw are not reached")
    void shouldThrowExceptionWhenMinimumTicketsNotReachedForDraw() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 2, ticketList);

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, () -> raffle.markAsDrawn(1L));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when winning ticket is not SOLD")
    void shouldThrowExceptionWhenWinningTicketIsNotSold() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        ticket2.assignToOwner("user-2");
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 2, ticketList);

        // ACT & ASSERT (ticket 3 is AVAILABLE, not SOLD)
        assertThrows(InvalidRaffleOperationException.class, () -> raffle.markAsDrawn(3L));
    }

    @Test
    @DisplayName("Should cancel ACTIVE raffle successfully")
    void shouldCancelActiveRaffle() {
        // ARRANGE
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 1, ticketList);

        // ACT
        raffle.cancel();

        // ASSERT
        assertEquals(RaffleStatus.CANCELLED, raffle.getStatus());
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when attempting to cancel a DRAWN raffle")
    void shouldThrowExceptionWhenCancellingDrawnRaffle() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        Raffle raffle = new Raffle("raf-001", "Villa", "Address", new BigDecimal("100000.00"), 1, ticketList);
        raffle.markAsDrawn(1L);

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, raffle::cancel);
    }
}
