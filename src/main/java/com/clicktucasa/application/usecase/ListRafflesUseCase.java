package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.repository.RaffleRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Single business flow: list the whole raffle catalogue. This is the entry
 * point the storefront calls on its very first render, so it is
 * deliberately total — no filtering, no pagination, no failure mode other
 * than the repository itself being unavailable — and it hands back an
 * unmodifiable view so no caller can mutate the catalogue in place.
 */
public class ListRafflesUseCase {

    private final RaffleRepository raffleRepository;

    public ListRafflesUseCase(RaffleRepository raffleRepository) {
        this.raffleRepository = Objects.requireNonNull(raffleRepository, "RaffleRepository cannot be null");
    }

    public List<Raffle> execute() {
        return Collections.unmodifiableList(raffleRepository.findAll());
    }
}
