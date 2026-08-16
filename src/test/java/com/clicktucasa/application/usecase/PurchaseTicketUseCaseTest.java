package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.entity.TicketStatus;
import com.clicktucasa.domain.exception.InvalidRaffleOperationException;
import com.clicktucasa.domain.exception.PaymentFailedException;
import com.clicktucasa.domain.exception.RaffleNotFoundException;
import com.clicktucasa.domain.exception.TicketNotAvailableException;
import com.clicktucasa.domain.port.PaymentGateway;
import com.clicktucasa.domain.repository.RaffleRepository;
import com.clicktucasa.domain.valueobject.HouseAddress;
import com.clicktucasa.domain.valueobject.HouseValue;
import com.clicktucasa.domain.valueobject.TicketPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseTicketUseCaseTest {

    @Mock
    private RaffleRepository raffleRepository;

    @Mock
    private PaymentGateway paymentGateway;

    private PurchaseTicketUseCase useCase;
    private Ticket ticket1;
    private Raffle raffle;

    @BeforeEach
    void setUp() {
        useCase = new PurchaseTicketUseCase(raffleRepository, paymentGateway);
        ticket1 = new Ticket(10L, new TicketPrice(new BigDecimal("100.00")));
        Ticket ticket2 = new Ticket(20L, new TicketPrice(new BigDecimal("100.00")));
        raffle = new Raffle("raf-999", "ClickTuCasa Grand Raffle", new HouseAddress("456 Beach Road"),
                new HouseValue(new BigDecimal("750000.00")), 2, List.of(ticket1, ticket2));
    }

    @Test
    @DisplayName("Should throw NullPointerException when constructed with null dependencies")
    void shouldThrowExceptionWhenConstructedWithNullDependencies() {
        assertThrows(NullPointerException.class, () -> new PurchaseTicketUseCase(null, paymentGateway));
        assertThrows(NullPointerException.class, () -> new PurchaseTicketUseCase(raffleRepository, null));
    }

    @Test
    @DisplayName("Should purchase an AVAILABLE ticket successfully when payment succeeds")
    void shouldPurchaseAvailableTicketSuccessfully() {
        // ARRANGE
        String userId = "user-bob";
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));
        when(paymentGateway.processPayment(userId, new BigDecimal("100.00"))).thenReturn(true);

        // ACT
        boolean result = useCase.execute("raf-999", 10L, userId);

        // ASSERT
        assertTrue(result);
        assertEquals(TicketStatus.SOLD, ticket1.getStatus());
        assertEquals(userId, ticket1.getOwnerId());
        verify(paymentGateway, times(1)).processPayment(userId, new BigDecimal("100.00"));
        verify(raffleRepository).save(raffle);
    }

    @Test
    @DisplayName("Should purchase a ticket RESERVED by the same user successfully")
    void shouldPurchaseReservedTicketBySameUserSuccessfully() {
        // ARRANGE
        String userId = "user-charlie";
        ticket1.reserve(userId, 10, LocalDateTime.now());
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));
        when(paymentGateway.processPayment(userId, new BigDecimal("100.00"))).thenReturn(true);

        // ACT
        boolean result = useCase.execute("raf-999", 10L, userId);

        // ASSERT
        assertTrue(result);
        assertEquals(TicketStatus.SOLD, ticket1.getStatus());
        assertEquals(userId, ticket1.getOwnerId());
    }

    @Test
    @DisplayName("Should throw TicketNotAvailableException when the ticket is reserved by another user")
    void shouldThrowExceptionWhenPurchasingTicketReservedByAnotherUser() {
        // ARRANGE
        ticket1.reserve("user-owner", 10, LocalDateTime.now());
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));

        // ACT & ASSERT
        assertThrows(TicketNotAvailableException.class, () -> useCase.execute("raf-999", 10L, "user-intruder"));
        verifyNoInteractions(paymentGateway);
    }

    @Test
    @DisplayName("Should throw TicketNotAvailableException when the ticket is already SOLD")
    void shouldThrowExceptionWhenPurchasingAlreadySoldTicket() {
        // ARRANGE
        ticket1.assignToOwner("user-owner");
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));

        // ACT & ASSERT
        assertThrows(TicketNotAvailableException.class, () -> useCase.execute("raf-999", 10L, "user-new"));
        verifyNoInteractions(paymentGateway);
    }

    @Test
    @DisplayName("Should throw PaymentFailedException and leave ticket state unchanged when payment is declined")
    void shouldThrowExceptionWhenPaymentDeclined() {
        // ARRANGE
        String userId = "user-dave";
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));
        when(paymentGateway.processPayment(userId, new BigDecimal("100.00"))).thenReturn(false);

        // ACT & ASSERT
        assertThrows(PaymentFailedException.class, () -> useCase.execute("raf-999", 10L, userId));
        assertEquals(TicketStatus.AVAILABLE, ticket1.getStatus());
        assertNull(ticket1.getOwnerId());
        verify(raffleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when inputs are invalid, without touching the repository")
    void shouldThrowExceptionWhenInputsAreInvalid() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, 10L, "user-1"));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(" ", 10L, "user-1"));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("raf-999", null, "user-1"));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("raf-999", 10L, null));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("raf-999", 10L, " "));
        verifyNoInteractions(raffleRepository);
    }

    @Test
    @DisplayName("Should throw RaffleNotFoundException when the repository has no raffle with that id")
    void shouldThrowExceptionWhenRaffleDoesNotExist() {
        when(raffleRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(RaffleNotFoundException.class, () -> useCase.execute("unknown", 10L, "user-1"));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when the raffle is not ACTIVE")
    void shouldThrowExceptionWhenPurchasingOnNonActiveRaffle() {
        // ARRANGE
        raffle.cancel();
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, () -> useCase.execute("raf-999", 10L, "user-1"));
        verifyNoInteractions(paymentGateway);
    }
}
