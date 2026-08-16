package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.entity.TicketStatus;
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

@ExtendWith(MockitoExtension.class)
class ReleaseExpiredReservationsUseCaseTest {

    @Mock
    private RaffleRepository raffleRepository;

    private ReleaseExpiredReservationsUseCase useCase;
    private Ticket ticket1;
    private Ticket ticket2;
    private Raffle raffle;

    @BeforeEach
    void setUp() {
        useCase = new ReleaseExpiredReservationsUseCase(raffleRepository);
        ticket1 = new Ticket(10L, new TicketPrice(new BigDecimal("100.00")));
        ticket2 = new Ticket(20L, new TicketPrice(new BigDecimal("100.00")));
        raffle = new Raffle("raf-999", "ClickTuCasa Grand Raffle", new HouseAddress("456 Beach Road"),
                new HouseValue(new BigDecimal("750000.00")), 2, List.of(ticket1, ticket2));
    }

    @Test
    @DisplayName("Should throw NullPointerException when constructed with a null repository")
    void shouldThrowExceptionWhenConstructedWithNullRepository() {
        assertThrows(NullPointerException.class, () -> new ReleaseExpiredReservationsUseCase(null));
    }

    @Test
    @DisplayName("Should release only expired reservations, persist the raffle and return the released count")
    void shouldReleaseExpiredReservations() {
        // ARRANGE
        LocalDateTime now = LocalDateTime.of(2026, 7, 26, 12, 0);
        ticket1.reserve("user-1", 10, now); // Expired at 12:10
        ticket2.reserve("user-2", 30, now); // Valid until 12:30
        LocalDateTime currentTime = now.plusMinutes(15); // 12:15
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));

        // ACT
        int releasedCount = useCase.execute("raf-999", currentTime);

        // ASSERT
        assertEquals(1, releasedCount);
        assertEquals(TicketStatus.AVAILABLE, ticket1.getStatus());
        assertNull(ticket1.getOwnerId());
        assertEquals(TicketStatus.RESERVED, ticket2.getStatus());
        assertEquals("user-2", ticket2.getOwnerId());
        verify(raffleRepository).save(raffle);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when inputs are invalid, without touching the repository")
    void shouldThrowExceptionWhenInputsAreInvalid() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(" ", LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("raf-999", null));
        verifyNoInteractions(raffleRepository);
    }

    @Test
    @DisplayName("Should throw RaffleNotFoundException when the repository has no raffle with that id")
    void shouldThrowExceptionWhenRaffleDoesNotExist() {
        when(raffleRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(RaffleNotFoundException.class, () -> useCase.execute("unknown", LocalDateTime.now()));
    }
}
