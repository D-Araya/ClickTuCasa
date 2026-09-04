package com.clicktucasa.infrastructure.persistence.entity;

import com.clicktucasa.domain.entity.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA persistence model for a single raffle ticket. This is deliberately a
 * plain "shell" for the relational row — it carries no business rules of
 * its own (no reserve()/assignToOwner() logic). All business behavior
 * lives in the domain's {@code Ticket}; {@link com.clicktucasa.infrastructure.persistence.RaffleRepositoryAdapter}
 * is the only class that translates between the two.
 */
@Entity
@Table(name = "tickets", uniqueConstraints = @UniqueConstraint(columnNames = {"raffle_id", "ticket_number"}))
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ticket_number", nullable = false)
    private Long ticketNumber;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @Column(name = "owner_id")
    private String ownerId;

    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raffle_id", nullable = false)
    private RaffleEntity raffle;

    protected TicketEntity() {
        // required by JPA
    }

    public TicketEntity(Long ticketNumber, BigDecimal price, TicketStatus status, String ownerId, LocalDateTime reservedUntil) {
        this.ticketNumber = ticketNumber;
        this.price = price;
        this.status = status;
        this.ownerId = ownerId;
        this.reservedUntil = reservedUntil;
    }

    public Long getId() {
        return id;
    }

    public Long getTicketNumber() {
        return ticketNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDateTime getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(LocalDateTime reservedUntil) {
        this.reservedUntil = reservedUntil;
    }

    public RaffleEntity getRaffle() {
        return raffle;
    }

    public void setRaffle(RaffleEntity raffle) {
        this.raffle = raffle;
    }
}
