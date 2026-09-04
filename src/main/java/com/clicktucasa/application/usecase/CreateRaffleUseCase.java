package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.repository.RaffleRepository;
import com.clicktucasa.domain.valueobject.HouseAddress;
import com.clicktucasa.domain.valueobject.HouseValue;
import com.clicktucasa.domain.valueobject.TicketPrice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Single business flow: create a brand-new raffle with a fixed pool of
 * sequentially numbered, freshly minted AVAILABLE tickets, all sharing the
 * same {@link TicketPrice}. This is the entry point that lets the Hito 4
 * REST API be exercised end to end (create, reserve/purchase, draw,
 * cancel) without needing pre-seeded data.
 */
public class CreateRaffleUseCase {

    private final RaffleRepository raffleRepository;

    public CreateRaffleUseCase(RaffleRepository raffleRepository) {
        this.raffleRepository = Objects.requireNonNull(raffleRepository, "RaffleRepository cannot be null");
    }

    public Raffle execute(String id, String title, HouseAddress houseAddress, HouseValue houseValue,
                           int minTicketsToDraw, int totalTickets, TicketPrice ticketPrice) {
        if (totalTickets <= 0) {
            throw new IllegalArgumentException("Total tickets must be positive");
        }
        if (ticketPrice == null) {
            throw new IllegalArgumentException("Ticket price cannot be null");
        }

        List<Ticket> tickets = new ArrayList<>(totalTickets);
        for (long number = 1; number <= totalTickets; number++) {
            tickets.add(new Ticket(number, ticketPrice));
        }

        Raffle raffle = new Raffle(id, title, houseAddress, houseValue, minTicketsToDraw, tickets);
        raffleRepository.save(raffle);
        return raffle;
    }
}
