package domain.service;

import domain.exception.InvalidRaffleOperationException;
import domain.exception.PaymentFailedException;
import domain.exception.TicketNotAvailableException;
import domain.model.Raffle;
import domain.model.Ticket;
import domain.model.TicketStatus;
import domain.port.PaymentGateway;
import domain.port.RandomNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaffleServiceTest {

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private RandomNumberGenerator randomNumberGenerator;

    private RaffleService raffleService;
    private Ticket ticket1;
    private Ticket ticket2;
    private Ticket ticket3;
    private Raffle raffle;

    @BeforeEach
    void setUp() {
        raffleService = new RaffleService(paymentGateway, randomNumberGenerator);
        ticket1 = new Ticket(10L, new BigDecimal("100.00"));
        ticket2 = new Ticket(20L, new BigDecimal("100.00"));
        ticket3 = new Ticket(30L, new BigDecimal("100.00"));
        raffle = new Raffle("raf-999", "ClickTuCasa Grand Raffle", "456 Beach Road", new BigDecimal("750000.00"), 2, List.of(ticket1, ticket2, ticket3));
    }

    @Test
    @DisplayName("Should throw NullPointerException when RaffleService is instantiated with null dependencies")
    void shouldThrowExceptionWhenServiceDependenciesAreNull() {
        // ARRANGE & ACT & ASSERT
        assertThrows(NullPointerException.class, () -> new RaffleService(null, randomNumberGenerator));
        assertThrows(NullPointerException.class, () -> new RaffleService(paymentGateway, null));
    }

    // --- RESERVE TICKET TESTS ---

    @Test
    @DisplayName("Should successfully reserve ticket when raffle is ACTIVE and ticket is AVAILABLE")
    void shouldReserveTicketSuccessfully() {
        // ARRANGE
        String userId = "user-alice";
        LocalDateTime now = LocalDateTime.of(2026, 7, 26, 12, 0);

        // ACT
        boolean result = raffleService.reserveTicket(raffle, 10L, userId, 15, now);

        // ASSERT
        assertTrue(result);
        assertEquals(TicketStatus.RESERVED, ticket1.getStatus());
        assertEquals(userId, ticket1.getOwnerId());
        assertEquals(now.plusMinutes(15), ticket1.getReservedUntil());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when reserveTicket receives invalid null or empty parameters")
    void shouldThrowExceptionWhenReserveTicketInputsAreInvalid() {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> raffleService.reserveTicket(null, 10L, "user-1", 10, now));
        assertThrows(IllegalArgumentException.class, () -> raffleService.reserveTicket(raffle, null, "user-1", 10, now));
        assertThrows(IllegalArgumentException.class, () -> raffleService.reserveTicket(raffle, 10L, null, 10, now));
        assertThrows(IllegalArgumentException.class, () -> raffleService.reserveTicket(raffle, 10L, "  ", 10, now));
        assertThrows(IllegalArgumentException.class, () -> raffleService.reserveTicket(raffle, 10L, "user-1", 10, null));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when trying to reserve ticket in non-ACTIVE raffle")
    void shouldThrowExceptionWhenReservingInNonActiveRaffle() {
        // ARRANGE
        raffle.cancel();

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, () -> raffleService.reserveTicket(raffle, 10L, "user-1", 10, LocalDateTime.now()));
    }

    // --- PURCHASE TICKET TESTS ---

    @Test
    @DisplayName("Should purchase ticket successfully when ticket is AVAILABLE and payment succeeds")
    void shouldPurchaseAvailableTicketSuccessfully() {
        // ARRANGE
        String userId = "user-bob";
        when(paymentGateway.processPayment(userId, new BigDecimal("100.00"))).thenReturn(true);

        // ACT
        boolean result = raffleService.purchaseTicket(raffle, 10L, userId);

        // ASSERT
        assertTrue(result);
        assertEquals(TicketStatus.SOLD, ticket1.getStatus());
        assertEquals(userId, ticket1.getOwnerId());
        verify(paymentGateway, times(1)).processPayment(userId, new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should purchase ticket successfully when ticket is RESERVED by the SAME user and payment succeeds")
    void shouldPurchaseReservedTicketBySameUserSuccessfully() {
        // ARRANGE
        String userId = "user-charlie";
        ticket1.reserve(userId, 10, LocalDateTime.now());
        when(paymentGateway.processPayment(userId, new BigDecimal("100.00"))).thenReturn(true);

        // ACT
        boolean result = raffleService.purchaseTicket(raffle, 10L, userId);

        // ASSERT
        assertTrue(result);
        assertEquals(TicketStatus.SOLD, ticket1.getStatus());
        assertEquals(userId, ticket1.getOwnerId());
        verify(paymentGateway, times(1)).processPayment(userId, new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should throw TicketNotAvailableException when purchasing a ticket RESERVED by another user")
    void shouldThrowExceptionWhenPurchasingTicketReservedByAnotherUser() {
        // ARRANGE
        ticket1.reserve("user-owner", 10, LocalDateTime.now());

        // ACT & ASSERT
        assertThrows(TicketNotAvailableException.class, () -> raffleService.purchaseTicket(raffle, 10L, "user-intruder"));
        verifyNoInteractions(paymentGateway);
    }

    @Test
    @DisplayName("Should throw TicketNotAvailableException when purchasing a ticket that is already SOLD")
    void shouldThrowExceptionWhenPurchasingAlreadySoldTicket() {
        // ARRANGE
        ticket1.assignToOwner("user-owner");

        // ACT & ASSERT
        assertThrows(TicketNotAvailableException.class, () -> raffleService.purchaseTicket(raffle, 10L, "user-new"));
        verifyNoInteractions(paymentGateway);
    }

    @Test
    @DisplayName("Should throw PaymentFailedException and leave ticket state unchanged when payment is declined")
    void shouldThrowExceptionWhenPaymentDeclined() {
        // ARRANGE
        String userId = "user-dave";
        when(paymentGateway.processPayment(userId, new BigDecimal("100.00"))).thenReturn(false);

        // ACT & ASSERT
        assertThrows(PaymentFailedException.class, () -> raffleService.purchaseTicket(raffle, 10L, userId));
        assertEquals(TicketStatus.AVAILABLE, ticket1.getStatus());
        assertNull(ticket1.getOwnerId());
        verify(paymentGateway, times(1)).processPayment(userId, new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when purchaseTicket receives invalid null or empty parameters")
    void shouldThrowExceptionWhenPurchaseTicketInputsAreInvalid() {
        // ARRANGE & ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> raffleService.purchaseTicket(null, 10L, "user-1"));
        assertThrows(IllegalArgumentException.class, () -> raffleService.purchaseTicket(raffle, null, "user-1"));
        assertThrows(IllegalArgumentException.class, () -> raffleService.purchaseTicket(raffle, 10L, null));
        assertThrows(IllegalArgumentException.class, () -> raffleService.purchaseTicket(raffle, 10L, " "));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when purchaseTicket is called on non-ACTIVE raffle")
    void shouldThrowExceptionWhenPurchasingOnNonActiveRaffle() {
        // ARRANGE
        raffle.cancel();

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, () -> raffleService.purchaseTicket(raffle, 10L, "user-1"));
        verifyNoInteractions(paymentGateway);
    }

    // --- RELEASE EXPIRED RESERVATIONS TESTS ---

    @Test
    @DisplayName("Should release only expired reservations and return count")
    void shouldReleaseExpiredReservations() {
        // ARRANGE
        LocalDateTime now = LocalDateTime.of(2026, 7, 26, 12, 0);
        ticket1.reserve("user-1", 10, now); // Expired at 12:10
        ticket2.reserve("user-2", 30, now); // Valid until 12:30

        LocalDateTime currentTime = now.plusMinutes(15); // 12:15 PM

        // ACT
        int releasedCount = raffleService.releaseExpiredReservations(raffle, currentTime);

        // ASSERT
        assertEquals(1, releasedCount);
        assertEquals(TicketStatus.AVAILABLE, ticket1.getStatus());
        assertNull(ticket1.getOwnerId());
        assertEquals(TicketStatus.RESERVED, ticket2.getStatus());
        assertEquals("user-2", ticket2.getOwnerId());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when releaseExpiredReservations receives null parameters")
    void shouldThrowExceptionWhenReleaseExpiredReservationsInputsAreNull() {
        // ARRANGE & ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> raffleService.releaseExpiredReservations(null, LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class, () -> raffleService.releaseExpiredReservations(raffle, null));
    }

    // --- DRAW WINNER TESTS ---

    @Test
    @DisplayName("Should successfully draw a winner using RandomNumberGenerator when criteria are met")
    void shouldDrawWinnerSuccessfully() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        ticket2.assignToOwner("user-2");
        when(randomNumberGenerator.generateRandomIndex(2)).thenReturn(1); // Selects ticket2 (index 1)

        // ACT
        Ticket winner = raffleService.drawWinner(raffle);

        // ASSERT
        assertNotNull(winner);
        assertEquals(20L, winner.getNumber());
        assertEquals("user-2", winner.getOwnerId());
        assertEquals(domain.model.RaffleStatus.DRAWN, raffle.getStatus());
        assertEquals(20L, raffle.getWinnerTicketNumber());
        verify(randomNumberGenerator, times(1)).generateRandomIndex(2);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when drawWinner receives null raffle")
    void shouldThrowExceptionWhenDrawWinnerReceivesNullRaffle() {
        // ARRANGE & ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> raffleService.drawWinner(null));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when drawWinner is called on raffle that cannot be drawn")
    void shouldThrowExceptionWhenRaffleCannotBeDrawn() {
        // ARRANGE (0 sold < 2 min required)

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, () -> raffleService.drawWinner(raffle));
        verifyNoInteractions(randomNumberGenerator);
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException if winning index generated is negative")
    void shouldThrowExceptionWhenWinningIndexIsNegative() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        ticket2.assignToOwner("user-2");
        when(randomNumberGenerator.generateRandomIndex(2)).thenReturn(-1); // Negative index

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, () -> raffleService.drawWinner(raffle));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException if winning index generated is greater than or equal to sold tickets size")
    void shouldThrowExceptionWhenWinningIndexIsOutOfBounds() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        ticket2.assignToOwner("user-2");
        when(randomNumberGenerator.generateRandomIndex(2)).thenReturn(5); // Invalid index

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, () -> raffleService.drawWinner(raffle));
    }
}
