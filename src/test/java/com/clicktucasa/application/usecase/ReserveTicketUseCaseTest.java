package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.entity.TicketStatus;
import com.clicktucasa.domain.exception.InvalidRaffleOperationException;
import com.clicktucasa.domain.exception.RaffleNotFoundException;
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

/**
 * Demonstrates Pilar 3: the use case is exercised purely against a mocked
 * {@link RaffleRepository} — no real database or infrastructure adapter is
 * needed to prove the business flow is correct.
 */
@ExtendWith(MockitoExtension.class)
class ReserveTicketUseCaseTest {

    @Mock
    private RaffleRepository raffleRepository;

    private ReserveTicketUseCase useCase;
    private Ticket ticket1;
    private Raffle raffle;

    @BeforeEach
    void setUp() {
        useCase = new ReserveTicketUseCase(raffleRepository);
        ticket1 = new Ticket(10L, new TicketPrice(new BigDecimal("100.00")));
        Ticket ticket2 = new Ticket(20L, new TicketPrice(new BigDecimal("100.00")));
        raffle = new Raffle("raf-999", "ClickTuCasa Grand Raffle", new HouseAddress("456 Beach Road"),
                new HouseValue(new BigDecimal("750000.00")), 2, List.of(ticket1, ticket2));
    }

    @Test
    @DisplayName("Should throw NullPointerException when constructed with a null repository")
    void shouldThrowExceptionWhenConstructedWithNullRepository() {
        assertThrows(NullPointerException.class, () -> new ReserveTicketUseCase(null));
    }

    @Test
    @DisplayName("Should reserve ticket and persist the raffle when raffle is ACTIVE and ticket is AVAILABLE")
    void shouldReserveTicketSuccessfully() {
        // ARRANGE
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));
        String userId = "user-alice";
        LocalDateTime now = LocalDateTime.of(2026, 7, 26, 12, 0);

        // ACT
        boolean result = useCase.execute("raf-999", 10L, userId, 15, now);

        // ASSERT
        assertTrue(result);
        assertEquals(TicketStatus.RESERVED, ticket1.getStatus());
        assertEquals(userId, ticket1.getOwnerId());
        assertEquals(now.plusMinutes(15), ticket1.getReservedUntil());
        verify(raffleRepository).save(raffle);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when inputs are invalid, without touching the repository")
    void shouldThrowExceptionWhenInputsAreInvalid() {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, 10L, "user-1", 10, now));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(" ", 10L, "user-1", 10, now));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("raf-999", null, "user-1", 10, now));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("raf-999", 10L, null, 10, now));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("raf-999", 10L, "  ", 10, now));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("raf-999", 10L, "user-1", 10, null));
        verifyNoInteractions(raffleRepository);
    }

    @Test
    @DisplayName("Should throw RaffleNotFoundException when the repository has no raffle with that id")
    void shouldThrowExceptionWhenRaffleDoesNotExist() {
        // ARRANGE
        when(raffleRepository.findById("unknown")).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RaffleNotFoundException.class, () -> useCase.execute("unknown", 10L, "user-1", 10, LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when trying to reserve a ticket in a non-ACTIVE raffle")
    void shouldThrowExceptionWhenReservingInNonActiveRaffle() {
        // ARRANGE
        raffle.cancel();
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class,
                () -> useCase.execute("raf-999", 10L, "user-1", 10, LocalDateTime.now()));
        verify(raffleRepository, never()).save(any());
    }
}
