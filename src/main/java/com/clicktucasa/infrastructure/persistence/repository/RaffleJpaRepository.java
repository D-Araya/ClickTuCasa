package com.clicktucasa.infrastructure.persistence.repository;

import com.clicktucasa.infrastructure.persistence.entity.RaffleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository: CRUD operations are resolved natively,
 * without a single manually written SQL statement (Pilar 2 of the Hito 4
 * rubric).
 */
@Repository
public interface RaffleJpaRepository extends JpaRepository<RaffleEntity, String> {
}
