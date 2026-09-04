package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.entity.TicketStatus;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRaffleUseCaseTest {

    @Mock
    private RaffleRepository raffleRepository;

    private CreateRaffleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateRaffleUseCase(raffleRepository);
    }

    @Test
    @DisplayName("Should throw NullPointerException when constructed with a null repository")
    void shouldThrowExceptionWhenConstructedWithNullRepository() {
        assertThrows(NullPointerException.class, () -> new CreateRaffleUseCase(null));
    }

    @Test
    @DisplayName("Should create a raffle with the requested number of AVAILABLE tickets and persist it")
    void shouldCreateRaffleSuccessfully() {
        // ARRANGE
        HouseAddress address = new HouseAddress("789 Sunset Blvd");
        HouseValue houseValue = new HouseValue(new BigDecimal("300000.00"));
        TicketPrice price = new TicketPrice(new BigDecimal("20.00"));

        // ACT
        Raffle raffle = useCase.execute("raf-100", "Beach House Raffle", address, houseValue, 3, 5, price);

        // ASSERT
        assertEquals("raf-100", raffle.getId());
        assertEquals(5, raffle.getTickets().size());
        assertTrue(raffle.getTickets().stream().allMatch(t -> t.getStatus() == TicketStatus.AVAILABLE));
        assertEquals(1L, raffle.getTickets().get(0).getNumber());
        assertEquals(5L, raffle.getTickets().get(4).getNumber());
        verify(raffleRepository).save(raffle);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when totalTickets is not positive")
    void shouldThrowExceptionWhenTotalTicketsIsNotPositive() {
        HouseAddress address = new HouseAddress("789 Sunset Blvd");
        HouseValue houseValue = new HouseValue(new BigDecimal("300000.00"));
        TicketPrice price = new TicketPrice(new BigDecimal("20.00"));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute("raf-101", "Raffle", address, houseValue, 1, 0, price));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("raf-101", "Raffle", address, houseValue, 1, -1, price));
        verifyNoInteractions(raffleRepository);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when ticketPrice is null")
    void shouldThrowExceptionWhenTicketPriceIsNull() {
        HouseAddress address = new HouseAddress("789 Sunset Blvd");
        HouseValue houseValue = new HouseValue(new BigDecimal("300000.00"));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute("raf-101", "Raffle", address, houseValue, 1, 3, null));
        verifyNoInteractions(raffleRepository);
    }
}
