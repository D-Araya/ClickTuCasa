package com.clicktucasa.infrastructure.persistence.entity;

import com.clicktucasa.domain.entity.RaffleStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence model for a raffle. Lives in
 * {@code infrastructure.persistence}, never in {@code domain} — the
 * annotations below (@Entity, @Table, @Id) are exactly the ones the Hito 4
 * rubric forbids on the domain's own {@code Raffle} class. This is a
 * "shell" that mirrors the raffle's storable state; every business rule
 * (can it be drawn? can it be cancelled?) still lives exclusively on the
 * domain entity.
 */
@Entity
@Table(name = "raffles")
public class RaffleEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "house_address", nullable = false)
    private String houseAddress;

    @Column(name = "house_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal houseValue;

    @Column(name = "min_tickets_to_draw", nullable = false)
    private int minTicketsToDraw;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RaffleStatus status;

    @Column(name = "winner_ticket_number")
    private Long winnerTicketNumber;

    @OneToMany(mappedBy = "raffle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TicketEntity> tickets = new ArrayList<>();

    protected RaffleEntity() {
        // required by JPA
    }

    public RaffleEntity(String id, String title, String houseAddress, BigDecimal houseValue,
                         int minTicketsToDraw, RaffleStatus status, Long winnerTicketNumber) {
        this.id = id;
        this.title = title;
        this.houseAddress = houseAddress;
        this.houseValue = houseValue;
        this.minTicketsToDraw = minTicketsToDraw;
        this.status = status;
        this.winnerTicketNumber = winnerTicketNumber;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHouseAddress() {
        return houseAddress;
    }

    public void setHouseAddress(String houseAddress) {
        this.houseAddress = houseAddress;
    }

    public BigDecimal getHouseValue() {
        return houseValue;
    }

    public void setHouseValue(BigDecimal houseValue) {
        this.houseValue = houseValue;
    }

    public int getMinTicketsToDraw() {
        return minTicketsToDraw;
    }

    public void setMinTicketsToDraw(int minTicketsToDraw) {
        this.minTicketsToDraw = minTicketsToDraw;
    }

    public RaffleStatus getStatus() {
        return status;
    }

    public void setStatus(RaffleStatus status) {
        this.status = status;
    }

    public Long getWinnerTicketNumber() {
        return winnerTicketNumber;
    }

    public void setWinnerTicketNumber(Long winnerTicketNumber) {
        this.winnerTicketNumber = winnerTicketNumber;
    }

    public List<TicketEntity> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketEntity> tickets) {
        this.tickets = tickets;
    }
}
