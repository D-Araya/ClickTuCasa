package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.Ticket;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListRafflesUseCaseTest {

    @Mock
    private RaffleRepository raffleRepository;

    private ListRafflesUseCase useCase;
    private Raffle firstRaffle;
    private Raffle secondRaffle;

    @BeforeEach
    void setUp() {
        useCase = new ListRafflesUseCase(raffleRepository);

        Ticket firstTicket = new Ticket(1L, new TicketPrice(new BigDecimal("10.00")));
        firstRaffle = new Raffle("raf-300", "Beach House Raffle", new HouseAddress("1 Ocean Ave"),
                new HouseValue(new BigDecimal("300000.00")), 1, List.of(firstTicket));

        Ticket secondTicket = new Ticket(1L, new TicketPrice(new BigDecimal("25.00")));
        secondRaffle = new Raffle("raf-301", "Mountain Cabin Raffle", new HouseAddress("9 Pine Road"),
                new HouseValue(new BigDecimal("150000.00")), 1, List.of(secondTicket));
    }

    @Test
    @DisplayName("Should throw NullPointerException when constructed with a null repository")
    void shouldThrowExceptionWhenConstructedWithNullRepository() {
        assertThrows(NullPointerException.class, () -> new ListRafflesUseCase(null));
    }

    @Test
    @DisplayName("Should return every raffle the repository holds")
    void shouldReturnEveryStoredRaffle() {
        when(raffleRepository.findAll()).thenReturn(List.of(firstRaffle, secondRaffle));

        List<Raffle> result = useCase.execute();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstRaffle));
        assertTrue(result.contains(secondRaffle));
        verify(raffleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return an empty list when the catalogue has no raffles")
    void shouldReturnEmptyListWhenCatalogueIsEmpty() {
        when(raffleRepository.findAll()).thenReturn(List.of());

        List<Raffle> result = useCase.execute();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return a read-only view so callers cannot mutate the catalogue")
    void shouldReturnUnmodifiableView() {
        when(raffleRepository.findAll()).thenReturn(new ArrayList<>(List.of(firstRaffle)));

        List<Raffle> result = useCase.execute();

        assertThrows(UnsupportedOperationException.class, () -> result.add(secondRaffle));
    }
}
