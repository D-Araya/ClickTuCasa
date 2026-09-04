package com.clicktucasa.domain.entity;

import com.clicktucasa.domain.exception.InvalidRaffleOperationException;
import com.clicktucasa.domain.exception.TicketNotFoundException;
import com.clicktucasa.domain.valueobject.HouseAddress;
import com.clicktucasa.domain.valueobject.HouseValue;
import com.clicktucasa.domain.valueobject.TicketPrice;
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
    private static final TicketPrice STANDARD_PRICE = new TicketPrice(new BigDecimal("50.00"));

    @BeforeEach
    void setUp() {
        ticket1 = new Ticket(1L, STANDARD_PRICE);
        ticket2 = new Ticket(2L, STANDARD_PRICE);
        ticket3 = new Ticket(3L, STANDARD_PRICE);
        ticketList = new ArrayList<>(List.of(ticket1, ticket2, ticket3));
    }

    @Test
    @DisplayName("Should create Raffle with ACTIVE status by default when valid parameters are provided")
    void shouldCreateRaffleSuccessfully() {
        // ARRANGE & ACT
        Raffle raffle = new Raffle("raf-001", "Luxury Villa Raffle", new HouseAddress("123 Ocean Drive"), new HouseValue(new BigDecimal("500000.00")), 2, ticketList);

        // ASSERT
        assertEquals("raf-001", raffle.getId());
        assertEquals("Luxury Villa Raffle", raffle.getTitle());
        assertEquals(new HouseAddress("123 Ocean Drive"), raffle.getHouseAddress());
        assertEquals(new HouseValue(new BigDecimal("500000.00")), raffle.getHouseValue());
        assertEquals(2, raffle.getMinTicketsToDraw());
        assertEquals(3, raffle.getTickets().size());
        assertEquals(RaffleStatus.ACTIVE, raffle.getStatus());
        assertNull(raffle.getWinnerTicketNumber());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when Raffle constructor arguments are invalid")
    void shouldThrowExceptionWhenConstructorArgsAreInvalid() {
        HouseAddress address = new HouseAddress("Address");
        HouseValue value = new HouseValue(new BigDecimal("100"));

        // ARRANGE & ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> new Raffle(null, "Title", address, value, 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle(" ", "Title", address, value, 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", null, address, value, 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "  ", address, value, 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", null, value, 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", address, null, 1, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", address, value, 0, ticketList));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", address, value, 1, null));
        assertThrows(IllegalArgumentException.class, () -> new Raffle("id", "Title", address, value, 1, List.of()));
    }

    @Test
    @DisplayName("Should find ticket by number successfully")
    void shouldFindTicketByNumber() {
        // ARRANGE
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 1, ticketList);

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
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 1, ticketList);

        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> raffle.findTicketByNumber(null));
    }

    @Test
    @DisplayName("Should throw TicketNotFoundException when ticket number does not exist in raffle")
    void shouldThrowExceptionWhenTicketNotFound() {
        // ARRANGE
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 1, ticketList);

        // ACT & ASSERT
        assertThrows(TicketNotFoundException.class, () -> raffle.findTicketByNumber(999L));
    }

    @Test
    @DisplayName("Should correctly categorize available, reserved, and sold tickets")
    void shouldCategorizeTicketsByStatus() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        ticket2.reserve("user-2", 10, java.time.LocalDateTime.now());
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 1, ticketList);

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
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 2, ticketList);

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
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 2, ticketList);
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
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 2, ticketList);

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
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 1, ticketList);
        raffle.cancel();

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, () -> raffle.markAsDrawn(1L));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when minimum tickets for draw are not reached")
    void shouldThrowExceptionWhenMinimumTicketsNotReachedForDraw() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 2, ticketList);

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, () -> raffle.markAsDrawn(1L));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when winning ticket is not SOLD")
    void shouldThrowExceptionWhenWinningTicketIsNotSold() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        ticket2.assignToOwner("user-2");
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 2, ticketList);

        // ACT & ASSERT (ticket 3 is AVAILABLE, not SOLD)
        assertThrows(InvalidRaffleOperationException.class, () -> raffle.markAsDrawn(3L));
    }

    @Test
    @DisplayName("Should cancel ACTIVE raffle successfully")
    void shouldCancelActiveRaffle() {
        // ARRANGE
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 1, ticketList);

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
        Raffle raffle = new Raffle("raf-001", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 1, ticketList);
        raffle.markAsDrawn(1L);

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, raffle::cancel);
    }

    @Test
    @DisplayName("Should reconstitute a DRAWN raffle with its winner ticket number")
    void shouldReconstituteDrawnRaffle() {
        // ARRANGE & ACT
        Raffle raffle = Raffle.reconstitute("raf-900", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 1, ticketList, RaffleStatus.DRAWN, 2L);

        // ASSERT
        assertEquals(RaffleStatus.DRAWN, raffle.getStatus());
        assertEquals(2L, raffle.getWinnerTicketNumber());
        assertEquals("raf-900", raffle.getId());
    }

    @Test
    @DisplayName("Should reconstitute an ACTIVE raffle with a null winner ticket number")
    void shouldReconstituteActiveRaffleWithoutWinner() {
        // ARRANGE & ACT
        Raffle raffle = Raffle.reconstitute("raf-901", "Villa", new HouseAddress("Address"), new HouseValue(new BigDecimal("100000.00")), 1, ticketList, RaffleStatus.ACTIVE, null);

        // ASSERT
        assertEquals(RaffleStatus.ACTIVE, raffle.getStatus());
        assertNull(raffle.getWinnerTicketNumber());
    }
}
