package com.clicktucasa.domain.repository;

import com.clicktucasa.domain.entity.Raffle;

import java.util.List;
import java.util.Optional;

/**
 * Pure storage contract for the Raffle aggregate. Lives entirely inside the
 * domain and operates only with domain objects — no persistence framework
 * (JPA, Spring Data, etc.) is referenced here. Concrete implementations
 * (e.g. backed by Postgres, an in-memory map, etc.) belong to
 * {@code infrastructure.persistence} and are injected into use cases
 * through their constructor, never instantiated with {@code new} inside
 * application logic.
 */
public interface RaffleRepository {

    void save(Raffle raffle);

    Optional<Raffle> findById(String id);

    /**
     * Every raffle currently stored, in no guaranteed order. Returns an
     * empty list — never {@code null} — when there is nothing stored, so
     * callers never have to null-check the catalogue.
     */
    List<Raffle> findAll();
}
