package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.exception.RaffleNotFoundException;
import com.clicktucasa.domain.repository.RaffleRepository;

import java.util.Objects;

/**
 * Single business flow: cancel an existing raffle (only valid before it
 * has been drawn — {@link Raffle#cancel()} enforces that rule).
 */
public class CancelRaffleUseCase {

    private final RaffleRepository raffleRepository;

    public CancelRaffleUseCase(RaffleRepository raffleRepository) {
        this.raffleRepository = Objects.requireNonNull(raffleRepository, "RaffleRepository cannot be null");
    }

    public void execute(String raffleId) {
        if (raffleId == null || raffleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Raffle ID cannot be empty");
        }
        Raffle raffle = raffleRepository.findById(raffleId)
                .orElseThrow(() -> new RaffleNotFoundException("Raffle " + raffleId + " not found"));
        raffle.cancel();
        raffleRepository.save(raffle);
    }
}
