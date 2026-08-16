package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.exception.InvalidRaffleOperationException;
import com.clicktucasa.domain.exception.RaffleNotFoundException;
import com.clicktucasa.domain.port.RandomNumberGenerator;
import com.clicktucasa.domain.repository.RaffleRepository;

import java.util.List;
import java.util.Objects;

/**
 * Single business flow: draw the winning ticket of a raffle once it is
 * eligible to be drawn, using the {@link RandomNumberGenerator} port.
 */
public class DrawWinnerUseCase {

    private final RaffleRepository raffleRepository;
    private final RandomNumberGenerator randomNumberGenerator;

    public DrawWinnerUseCase(RaffleRepository raffleRepository, RandomNumberGenerator randomNumberGenerator) {
        this.raffleRepository = Objects.requireNonNull(raffleRepository, "RaffleRepository cannot be null");
        this.randomNumberGenerator = Objects.requireNonNull(randomNumberGenerator, "RandomNumberGenerator cannot be null");
    }

    public Ticket execute(String raffleId) {
        if (raffleId == null || raffleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Raffle ID cannot be empty");
        }

        Raffle raffle = raffleRepository.findById(raffleId)
                .orElseThrow(() -> new RaffleNotFoundException("Raffle " + raffleId + " not found"));

        if (!raffle.canBeDrawn()) {
            throw new InvalidRaffleOperationException("Raffle cannot be drawn: criteria not met");
        }

        List<Ticket> soldTickets = raffle.getSoldTickets();

        int winningIndex = randomNumberGenerator.generateRandomIndex(soldTickets.size());
        if (winningIndex < 0 || winningIndex >= soldTickets.size()) {
            throw new InvalidRaffleOperationException("Generated winning index out of bounds");
        }

        Ticket winningTicket = soldTickets.get(winningIndex);
        raffle.markAsDrawn(winningTicket.getNumber());
        raffleRepository.save(raffle);
        return winningTicket;
    }
}
