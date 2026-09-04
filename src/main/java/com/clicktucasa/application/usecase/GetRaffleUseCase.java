package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.exception.RaffleNotFoundException;
import com.clicktucasa.domain.repository.RaffleRepository;

import java.util.Objects;

/**
 * Single business flow: retrieve a raffle by id. Kept as its own use case
 * — rather than letting the web controller call {@link RaffleRepository}
 * directly — so every application-layer entry point follows the same
 * "one use case per flow" convention introduced for the other four flows.
 */
public class GetRaffleUseCase {

    private final RaffleRepository raffleRepository;

    public GetRaffleUseCase(RaffleRepository raffleRepository) {
        this.raffleRepository = Objects.requireNonNull(raffleRepository, "RaffleRepository cannot be null");
    }

    public Raffle execute(String raffleId) {
        if (raffleId == null || raffleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Raffle ID cannot be empty");
        }
        return raffleRepository.findById(raffleId)
                .orElseThrow(() -> new RaffleNotFoundException("Raffle " + raffleId + " not found"));
    }
}
