/**
 * Concrete implementations of {@link com.clicktucasa.domain.repository.RaffleRepository}
 * belong here (e.g. a JPA/Postgres adapter, an in-memory adapter for local
 * development, etc.).
 *
 * This package is intentionally left without a concrete implementation for
 * the Hito 3 deliverable: wiring a real Spring Boot + database adapter is
 * scope for Unidad 4. Pilar 3 of this Hito is satisfied without it, since
 * the use cases in {@code application.usecase} depend only on the
 * {@code RaffleRepository} interface (injected by constructor) and are
 * fully tested with Mockito against that abstraction — no real database is
 * required for the domain and application layers to be complete and
 * verifiable.
 */
package com.clicktucasa.infrastructure.persistence;
