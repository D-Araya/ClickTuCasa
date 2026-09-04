/**
 * Concrete implementation of {@link com.clicktucasa.domain.repository.RaffleRepository},
 * completed in the Hito 4 deliverable: {@link com.clicktucasa.infrastructure.persistence.RaffleRepositoryAdapter}
 * backs the domain's pure repository port with Spring Data JPA and
 * PostgreSQL. The {@code entity} subpackage holds the JPA "shell" classes
 * ({@code RaffleEntity}, {@code TicketEntity}) and the {@code repository}
 * subpackage holds the generated {@code JpaRepository}; neither the
 * domain nor the application layer ever references them directly.
 */
package com.clicktucasa.infrastructure.persistence;
