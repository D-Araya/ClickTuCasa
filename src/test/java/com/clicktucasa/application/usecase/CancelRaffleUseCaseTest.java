package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.RaffleStatus;
import com.clicktucasa.domain.entity.Ticket;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelRaffleUseCaseTest {

    @Mock
    private RaffleRepository raffleRepository;

    private CancelRaffleUseCase useCase;
    private Raffle raffle;

    @BeforeEach
    void setUp() {
        useCase = new CancelRaffleUseCase(raffleRepository);
        Ticket ticket = new Ticket(1L, new TicketPrice(new BigDecimal("10.00")));
        raffle = new Raffle("raf-300", "Cabin Raffle", new HouseAddress("2 Lake Rd"),
                new HouseValue(new BigDecimal("150000.00")), 1, List.of(ticket));
    }

    @Test
    @DisplayName("Should throw NullPointerException when constructed with a null repository")
    void shouldThrowExceptionWhenConstructedWithNullRepository() {
        assertThrows(NullPointerException.class, () -> new CancelRaffleUseCase(null));
    }

    @Test
    @DisplayName("Should cancel an ACTIVE raffle and persist it")
    void shouldCancelRaffleSuccessfully() {
        when(raffleRepository.findById("raf-300")).thenReturn(Optional.of(raffle));

        useCase.execute("raf-300");

        assertEquals(RaffleStatus.CANCELLED, raffle.getStatus());
        verify(raffleRepository).save(raffle);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when raffleId is empty, without touching the repository")
    void shouldThrowExceptionWhenRaffleIdIsEmpty() {
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
}
