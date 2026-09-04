package com.clicktucasa.infrastructure.persistence;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.repository.RaffleRepository;
import com.clicktucasa.domain.valueobject.HouseAddress;
import com.clicktucasa.domain.valueobject.HouseValue;
import com.clicktucasa.domain.valueobject.TicketPrice;
import com.clicktucasa.infrastructure.persistence.entity.RaffleEntity;
import com.clicktucasa.infrastructure.persistence.entity.TicketEntity;
import com.clicktucasa.infrastructure.persistence.repository.RaffleJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that satisfies the pure {@link RaffleRepository} port defined in
 * the domain, using Spring Data JPA underneath. This is the only class in
 * the whole codebase allowed to know both the domain model
 * (Raffle/Ticket) and the persistence model (RaffleEntity/TicketEntity);
 * every other class talks exclusively to one side or the other.
 *
 * <p>On update, an already-persisted {@link RaffleEntity} is loaded and
 * mutated in place (ticket rows matched by their business
 * {@code ticketNumber}) rather than replaced by a brand-new detached
 * graph, so a reservation or purchase does not needlessly recreate every
 * ticket row on every save.
 */
@Repository
public class RaffleRepositoryAdapter implements RaffleRepository {

    private final RaffleJpaRepository jpaRepository;

    public RaffleRepositoryAdapter(RaffleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Raffle raffle) {
        RaffleEntity entity = jpaRepository.findById(raffle.getId())
                .map(existing -> mergeIntoEntity(existing, raffle))
                .orElseGet(() -> toNewEntity(raffle));
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Raffle> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private RaffleEntity toNewEntity(Raffle raffle) {
        RaffleEntity entity = new RaffleEntity(
                raffle.getId(),
                raffle.getTitle(),
                raffle.getHouseAddress().value(),
                raffle.getHouseValue().amount(),
                raffle.getMinTicketsToDraw(),
                raffle.getStatus(),
                raffle.getWinnerTicketNumber());

        List<TicketEntity> ticketEntities = raffle.getTickets().stream()
                .map(ticket -> newTicketEntity(ticket, entity))
                .collect(Collectors.toList());
        entity.setTickets(ticketEntities);
        return entity;
    }

    private TicketEntity newTicketEntity(Ticket ticket, RaffleEntity owner) {
        TicketEntity ticketEntity = new TicketEntity(
                ticket.getNumber(),
                ticket.getPrice().amount(),
                ticket.getStatus(),
                ticket.getOwnerId(),
                ticket.getReservedUntil());
        ticketEntity.setRaffle(owner);
        return ticketEntity;
    }

    private RaffleEntity mergeIntoEntity(RaffleEntity existing, Raffle raffle) {
        existing.setTitle(raffle.getTitle());
        existing.setHouseAddress(raffle.getHouseAddress().value());
        existing.setHouseValue(raffle.getHouseValue().amount());
        existing.setMinTicketsToDraw(raffle.getMinTicketsToDraw());
        existing.setStatus(raffle.getStatus());
        existing.setWinnerTicketNumber(raffle.getWinnerTicketNumber());

        Map<Long, TicketEntity> existingByNumber = new HashMap<>();
        for (TicketEntity ticketEntity : existing.getTickets()) {
            existingByNumber.put(ticketEntity.getTicketNumber(), ticketEntity);
        }

        for (Ticket ticket : raffle.getTickets()) {
            TicketEntity ticketEntity = existingByNumber.get(ticket.getNumber());
            if (ticketEntity == null) {
                // Defensive path: the ticket pool is fixed at creation time
                // (CreateRaffleUseCase), so this only triggers if a raffle
                // was mutated to add tickets after the fact.
                existing.getTickets().add(newTicketEntity(ticket, existing));
            } else {
                ticketEntity.setStatus(ticket.getStatus());
                ticketEntity.setOwnerId(ticket.getOwnerId());
                ticketEntity.setReservedUntil(ticket.getReservedUntil());
            }
        }
        return existing;
    }

    private Raffle toDomain(RaffleEntity entity) {
        List<Ticket> tickets = entity.getTickets().stream()
                .map(ticketEntity -> Ticket.reconstitute(
                        ticketEntity.getTicketNumber(),
                        new TicketPrice(ticketEntity.getPrice()),
                        ticketEntity.getStatus(),
                        ticketEntity.getOwnerId(),
                        ticketEntity.getReservedUntil()))
                .collect(Collectors.toList());

        return Raffle.reconstitute(
                entity.getId(),
                entity.getTitle(),
                new HouseAddress(entity.getHouseAddress()),
                new HouseValue(entity.getHouseValue()),
                entity.getMinTicketsToDraw(),
                tickets,
                entity.getStatus(),
                entity.getWinnerTicketNumber());
    }
}
