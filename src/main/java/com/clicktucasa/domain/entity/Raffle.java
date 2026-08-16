package com.clicktucasa.domain.entity;

import com.clicktucasa.domain.exception.InvalidRaffleOperationException;
import com.clicktucasa.domain.exception.TicketNotFoundException;
import com.clicktucasa.domain.valueobject.HouseAddress;
import com.clicktucasa.domain.valueobject.HouseValue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Raffle {
    private final String id;
    private final String title;
    private final HouseAddress houseAddress;
    private final HouseValue houseValue;
    private final int minTicketsToDraw;
    private final List<Ticket> tickets;
    private RaffleStatus status;
    private Long winnerTicketNumber;

    public Raffle(String id, String title, HouseAddress houseAddress, HouseValue houseValue, int minTicketsToDraw, List<Ticket> tickets) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Raffle ID cannot be empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Raffle title cannot be empty");
        }
        if (houseAddress == null) {
            throw new IllegalArgumentException("House address cannot be null");
        }
        if (houseValue == null) {
            throw new IllegalArgumentException("House value cannot be null");
        }
        if (minTicketsToDraw <= 0) {
            throw new IllegalArgumentException("Minimum tickets to draw must be positive");
        }
        if (tickets == null || tickets.isEmpty()) {
            throw new IllegalArgumentException("Raffle must contain at least one ticket");
        }
        this.id = id;
        this.title = title;
        this.houseAddress = houseAddress;
        this.houseValue = houseValue;
        this.minTicketsToDraw = minTicketsToDraw;
        this.tickets = tickets;
        this.status = RaffleStatus.ACTIVE;
        this.winnerTicketNumber = null;
    }

    public Ticket findTicketByNumber(Long ticketNumber) {
        if (ticketNumber == null) {
            throw new IllegalArgumentException("Ticket number cannot be null");
        }
        return tickets.stream()
                .filter(t -> t.getNumber().equals(ticketNumber))
                .findFirst()
                .orElseThrow(() -> new TicketNotFoundException("Ticket number " + ticketNumber + " not found in raffle " + id));
    }

    public List<Ticket> getAvailableTickets() {
        return tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public List<Ticket> getReservedTickets() {
        return tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.RESERVED)
                .collect(Collectors.toList());
    }

    public List<Ticket> getSoldTickets() {
        return tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.SOLD)
                .collect(Collectors.toList());
    }

    public boolean canBeDrawn() {
        return this.status == RaffleStatus.ACTIVE && getSoldTickets().size() >= minTicketsToDraw;
    }

    public void markAsDrawn(Long winnerTicketNumber) {
        if (this.status != RaffleStatus.ACTIVE) {
            throw new InvalidRaffleOperationException("Cannot draw winner because raffle is " + status);
        }
        if (!canBeDrawn()) {
            throw new InvalidRaffleOperationException("Cannot draw winner because minimum required tickets (" + minTicketsToDraw + ") were not sold");
        }
        Ticket winnerTicket = findTicketByNumber(winnerTicketNumber);
        if (winnerTicket.getStatus() != TicketStatus.SOLD) {
            throw new InvalidRaffleOperationException("Winning ticket must be SOLD");
        }
        this.status = RaffleStatus.DRAWN;
        this.winnerTicketNumber = winnerTicketNumber;
    }

    public void cancel() {
        if (this.status == RaffleStatus.DRAWN) {
            throw new InvalidRaffleOperationException("Cannot cancel a raffle that has already been DRAWN");
        }
        this.status = RaffleStatus.CANCELLED;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public HouseAddress getHouseAddress() {
        return houseAddress;
    }

    public HouseValue getHouseValue() {
        return houseValue;
    }

    public int getMinTicketsToDraw() {
        return minTicketsToDraw;
    }

    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    public RaffleStatus getStatus() {
        return status;
    }

    public Long getWinnerTicketNumber() {
        return winnerTicketNumber;
    }
}
