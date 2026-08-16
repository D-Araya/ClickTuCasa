package com.clicktucasa.domain.entity;

import com.clicktucasa.domain.exception.TicketNotAvailableException;
import com.clicktucasa.domain.valueobject.TicketPrice;

import java.time.LocalDateTime;

public class Ticket {
    private final Long number;
    private final TicketPrice price;
    private TicketStatus status;
    private String ownerId;
    private LocalDateTime reservedUntil;

    public Ticket(Long number, TicketPrice price) {
        this(number, price, TicketStatus.AVAILABLE);
    }

    public Ticket(Long number, TicketPrice price, TicketStatus status) {
        if (number == null || number <= 0) {
            throw new IllegalArgumentException("Ticket number must be positive");
        }
        if (price == null) {
            throw new IllegalArgumentException("Ticket price cannot be null");
        }
        this.number = number;
        this.price = price;
        this.status = status != null ? status : TicketStatus.AVAILABLE;
        this.ownerId = null;
        this.reservedUntil = null;
    }

    public void reserve(String userId, int durationMinutes, LocalDateTime currentTime) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Reservation duration must be positive");
        }
        if (this.status != TicketStatus.AVAILABLE) {
            throw new TicketNotAvailableException("Cannot reserve ticket " + number + " because status is " + status);
        }
        this.status = TicketStatus.RESERVED;
        this.ownerId = userId;
        this.reservedUntil = currentTime.plusMinutes(durationMinutes);
    }

    public boolean isReservationExpired(LocalDateTime currentTime) {
        if (this.status != TicketStatus.RESERVED || this.reservedUntil == null) {
            return false;
        }
        return currentTime.isAfter(this.reservedUntil);
    }

    public void releaseReservation() {
        if (this.status == TicketStatus.RESERVED) {
            this.status = TicketStatus.AVAILABLE;
            this.ownerId = null;
            this.reservedUntil = null;
        }
    }

    public void assignToOwner(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner ID cannot be empty");
        }
        if (this.status == TicketStatus.SOLD) {
            throw new TicketNotAvailableException("Cannot assign ticket " + number + " because it is already SOLD");
        }
        this.status = TicketStatus.SOLD;
        this.ownerId = userId;
        this.reservedUntil = null;
    }

    public Long getNumber() {
        return number;
    }

    public TicketPrice getPrice() {
        return price;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getReservedUntil() {
        return reservedUntil;
    }
}
