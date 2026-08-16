package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.RaffleStatus;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.exception.InvalidRaffleOperationException;
import com.clicktucasa.domain.exception.RaffleNotFoundException;
import com.clicktucasa.domain.port.RandomNumberGenerator;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrawWinnerUseCaseTest {

    @Mock
    private RaffleRepository raffleRepository;

    @Mock
    private RandomNumberGenerator randomNumberGenerator;

    private DrawWinnerUseCase useCase;
    private Ticket ticket1;
    private Ticket ticket2;
    private Raffle raffle;

    @BeforeEach
    void setUp() {
        useCase = new DrawWinnerUseCase(raffleRepository, randomNumberGenerator);
        ticket1 = new Ticket(10L, new TicketPrice(new BigDecimal("100.00")));
        ticket2 = new Ticket(20L, new TicketPrice(new BigDecimal("100.00")));
        raffle = new Raffle("raf-999", "ClickTuCasa Grand Raffle", new HouseAddress("456 Beach Road"),
                new HouseValue(new BigDecimal("750000.00")), 2, List.of(ticket1, ticket2));
    }

    @Test
    @DisplayName("Should throw NullPointerException when constructed with null dependencies")
    void shouldThrowExceptionWhenConstructedWithNullDependencies() {
        assertThrows(NullPointerException.class, () -> new DrawWinnerUseCase(null, randomNumberGenerator));
        assertThrows(NullPointerException.class, () -> new DrawWinnerUseCase(raffleRepository, null));
    }

    @Test
    @DisplayName("Should draw a winner using RandomNumberGenerator and persist the raffle when criteria are met")
    void shouldDrawWinnerSuccessfully() {
        // ARRANGE
        ticket1.assignToOwner("user-1");
        ticket2.assignToOwner("user-2");
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));
        when(randomNumberGenerator.generateRandomIndex(2)).thenReturn(1); // selects ticket2

        // ACT
        Ticket winner = useCase.execute("raf-999");

        // ASSERT
        assertNotNull(winner);
        assertEquals(20L, winner.getNumber());
        assertEquals("user-2", winner.getOwnerId());
        assertEquals(RaffleStatus.DRAWN, raffle.getStatus());
        assertEquals(20L, raffle.getWinnerTicketNumber());
        verify(randomNumberGenerator, times(1)).generateRandomIndex(2);
        verify(raffleRepository).save(raffle);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when raffleId is invalid, without touching the repository")
    void shouldThrowExceptionWhenRaffleIdIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(" "));
        verifyNoInteractions(raffleRepository);
    }

    @Test
    @DisplayName("Should throw RaffleNotFoundException when the repository has no raffle with that id")
    void shouldThrowExceptionWhenRaffleDoesNotExist() {
        when(raffleRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(RaffleNotFoundException.class, () -> useCase.execute("unknown"));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when the raffle cannot be drawn yet")
    void shouldThrowExceptionWhenRaffleCannotBeDrawn() {
        // ARRANGE (0 sold < 2 min required)
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));

        // ACT & ASSERT
        assertThrows(InvalidRaffleOperationException.class, () -> useCase.execute("raf-999"));
        verifyNoInteractions(randomNumberGenerator);
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when the generated winning index is negative")
    void shouldThrowExceptionWhenWinningIndexIsNegative() {
        ticket1.assignToOwner("user-1");
        ticket2.assignToOwner("user-2");
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));
        when(randomNumberGenerator.generateRandomIndex(2)).thenReturn(-1);

        assertThrows(InvalidRaffleOperationException.class, () -> useCase.execute("raf-999"));
    }

    @Test
    @DisplayName("Should throw InvalidRaffleOperationException when the generated winning index is out of bounds")
    void shouldThrowExceptionWhenWinningIndexIsOutOfBounds() {
        ticket1.assignToOwner("user-1");
        ticket2.assignToOwner("user-2");
        when(raffleRepository.findById("raf-999")).thenReturn(Optional.of(raffle));
        when(randomNumberGenerator.generateRandomIndex(2)).thenReturn(5);

        assertThrows(InvalidRaffleOperationException.class, () -> useCase.execute("raf-999"));
    }
}
