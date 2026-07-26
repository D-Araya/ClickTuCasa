package domain.service;

import domain.exception.InvalidRaffleOperationException;
import domain.exception.PaymentFailedException;
import domain.exception.TicketNotAvailableException;
import domain.model.Raffle;
import domain.model.RaffleStatus;
import domain.model.Ticket;
import domain.model.TicketStatus;
import domain.port.PaymentGateway;
import domain.port.RandomNumberGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class RaffleService {
    private final PaymentGateway paymentGateway;
    private final RandomNumberGenerator randomNumberGenerator;

    public RaffleService(PaymentGateway paymentGateway, RandomNumberGenerator randomNumberGenerator) {
        this.paymentGateway = Objects.requireNonNull(paymentGateway, "PaymentGateway cannot be null");
        this.randomNumberGenerator = Objects.requireNonNull(randomNumberGenerator, "RandomNumberGenerator cannot be null");
    }

    public boolean reserveTicket(Raffle raffle, Long ticketNumber, String userId, int durationMinutes, LocalDateTime currentTime) {
        validateInputs(raffle, ticketNumber, userId, currentTime);
        if (raffle.getStatus() != RaffleStatus.ACTIVE) {
            throw new InvalidRaffleOperationException("Cannot reserve ticket because raffle is " + raffle.getStatus());
        }
        Ticket ticket = raffle.findTicketByNumber(ticketNumber);
        ticket.reserve(userId, durationMinutes, currentTime);
        return true;
    }

    public boolean purchaseTicket(Raffle raffle, Long ticketNumber, String userId) {
        if (raffle == null) {
            throw new IllegalArgumentException("Raffle cannot be null");
        }
        if (ticketNumber == null) {
            throw new IllegalArgumentException("Ticket number cannot be null");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (raffle.getStatus() != RaffleStatus.ACTIVE) {
            throw new InvalidRaffleOperationException("Cannot purchase ticket because raffle is " + raffle.getStatus());
        }

        Ticket ticket = raffle.findTicketByNumber(ticketNumber);

        if (ticket.getStatus() == TicketStatus.RESERVED && !userId.equals(ticket.getOwnerId())) {
            throw new TicketNotAvailableException("Ticket " + ticketNumber + " is reserved by another user");
        }
        if (ticket.getStatus() == TicketStatus.SOLD) {
            throw new TicketNotAvailableException("Ticket " + ticketNumber + " is already SOLD");
        }

        boolean paid = paymentGateway.processPayment(userId, ticket.getPrice());
        if (!paid) {
            throw new PaymentFailedException("Payment failed for user " + userId + " on ticket " + ticketNumber);
        }

        ticket.assignToOwner(userId);
        return true;
    }

    public int releaseExpiredReservations(Raffle raffle, LocalDateTime currentTime) {
        if (raffle == null) {
            throw new IllegalArgumentException("Raffle cannot be null");
        }
        if (currentTime == null) {
            throw new IllegalArgumentException("Current time cannot be null");
        }

        List<Ticket> reservedTickets = raffle.getReservedTickets();
        int releasedCount = 0;
        for (Ticket ticket : reservedTickets) {
            if (ticket.isReservationExpired(currentTime)) {
                ticket.releaseReservation();
                releasedCount++;
            }
        }
        return releasedCount;
    }

    public Ticket drawWinner(Raffle raffle) {
        if (raffle == null) {
            throw new IllegalArgumentException("Raffle cannot be null");
        }
        if (!raffle.canBeDrawn()) {
            throw new InvalidRaffleOperationException("Raffle cannot be drawn: criteria not met");
        }

        List<Ticket> soldTickets = raffle.getSoldTickets();

        int winningIndex = randomNumberGenerator.generateRandomIndex(soldTickets.size());
        if (winningIndex < 0 || winningIndex >= soldTickets.size()) {
            throw new InvalidRaffleOperationException("Generated winning index out of bounds");
        }

        Ticket winningTicket = soldTickets.get(winningIndex);
        raffle.markAsDrawn(winningTicket.getNumber());
        return winningTicket;
    }

    private void validateInputs(Raffle raffle, Long ticketNumber, String userId, LocalDateTime currentTime) {
        if (raffle == null) {
            throw new IllegalArgumentException("Raffle cannot be null");
        }
        if (ticketNumber == null) {
            throw new IllegalArgumentException("Ticket number cannot be null");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (currentTime == null) {
            throw new IllegalArgumentException("Current time cannot be null");
        }
    }
}
