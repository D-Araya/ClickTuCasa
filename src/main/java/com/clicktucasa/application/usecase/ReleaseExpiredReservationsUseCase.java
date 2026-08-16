package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.exception.RaffleNotFoundException;
import com.clicktucasa.domain.repository.RaffleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Single business flow: release ticket reservations that have expired for
 * a given raffle, freeing them up for other buyers.
 */
public class ReleaseExpiredReservationsUseCase {

    private final RaffleRepository raffleRepository;

    public ReleaseExpiredReservationsUseCase(RaffleRepository raffleRepository) {
        this.raffleRepository = Objects.requireNonNull(raffleRepository, "RaffleRepository cannot be null");
    }

    public int execute(String raffleId, LocalDateTime currentTime) {
        if (raffleId == null || raffleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Raffle ID cannot be empty");
        }
        if (currentTime == null) {
            throw new IllegalArgumentException("Current time cannot be null");
        }

        Raffle raffle = raffleRepository.findById(raffleId)
                .orElseThrow(() -> new RaffleNotFoundException("Raffle " + raffleId + " not found"));

        List<Ticket> reservedTickets = raffle.getReservedTickets();
        int releasedCount = 0;
        for (Ticket ticket : reservedTickets) {
            if (ticket.isReservationExpired(currentTime)) {
                ticket.releaseReservation();
                releasedCount++;
            }
        }

        raffleRepository.save(raffle);
        return releasedCount;
    }
}
