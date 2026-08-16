package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.RaffleStatus;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.exception.InvalidRaffleOperationException;
import com.clicktucasa.domain.exception.RaffleNotFoundException;
import com.clicktucasa.domain.repository.RaffleRepository;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Single business flow: reserve a raffle ticket for a user for a limited
 * time window. Depends only on the {@link RaffleRepository} abstraction,
 * injected through the constructor — never instantiates a concrete
 * repository implementation itself.
 */
public class ReserveTicketUseCase {

    private final RaffleRepository raffleRepository;

    public ReserveTicketUseCase(RaffleRepository raffleRepository) {
        this.raffleRepository = Objects.requireNonNull(raffleRepository, "RaffleRepository cannot be null");
    }

    public boolean execute(String raffleId, Long ticketNumber, String userId, int durationMinutes, LocalDateTime currentTime) {
        if (raffleId == null || raffleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Raffle ID cannot be empty");
        }
        if (ticketNumber == null) {
            throw new IllegalArgumentException("Ticket number cannot be null");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (currentTime == null) {
            throw new IllegalArgumentException("Current time cannot be null");
        }

        Raffle raffle = raffleRepository.findById(raffleId)
                .orElseThrow(() -> new RaffleNotFoundException("Raffle " + raffleId + " not found"));

        if (raffle.getStatus() != RaffleStatus.ACTIVE) {
            throw new InvalidRaffleOperationException("Cannot reserve ticket because raffle is " + raffle.getStatus());
        }

        Ticket ticket = raffle.findTicketByNumber(ticketNumber);
        ticket.reserve(userId, durationMinutes, currentTime);

        raffleRepository.save(raffle);
        return true;
    }
}
