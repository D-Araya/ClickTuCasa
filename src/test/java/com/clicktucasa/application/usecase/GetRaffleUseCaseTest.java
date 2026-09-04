package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
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
class GetRaffleUseCaseTest {

    @Mock
    private RaffleRepository raffleRepository;

    private GetRaffleUseCase useCase;
    private Raffle raffle;

    @BeforeEach
    void setUp() {
        useCase = new GetRaffleUseCase(raffleRepository);
        Ticket ticket = new Ticket(1L, new TicketPrice(new BigDecimal("10.00")));
        raffle = new Raffle("raf-200", "Condo Raffle", new HouseAddress("1 Main St"),
                new HouseValue(new BigDecimal("200000.00")), 1, List.of(ticket));
    }

    @Test
    @DisplayName("Should throw NullPointerException when constructed with a null repository")
    void shouldThrowExceptionWhenConstructedWithNullRepository() {
        assertThrows(NullPointerException.class, () -> new GetRaffleUseCase(null));
    }

    @Test
    @DisplayName("Should return the raffle when it exists")
    void shouldReturnRaffleWhenItExists() {
        when(raffleRepository.findById("raf-200")).thenReturn(Optional.of(raffle));

        Raffle result = useCase.execute("raf-200");

        assertEquals(raffle, result);
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
