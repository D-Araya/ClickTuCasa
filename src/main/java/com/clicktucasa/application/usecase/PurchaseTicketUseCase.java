package com.clicktucasa.application.usecase;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.RaffleStatus;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.entity.TicketStatus;
import com.clicktucasa.domain.exception.InvalidRaffleOperationException;
import com.clicktucasa.domain.exception.PaymentFailedException;
import com.clicktucasa.domain.exception.RaffleNotFoundException;
import com.clicktucasa.domain.exception.TicketNotAvailableException;
import com.clicktucasa.domain.port.PaymentGateway;
import com.clicktucasa.domain.repository.RaffleRepository;

import java.util.Objects;

/**
 * Single business flow: purchase a raffle ticket, charging the buyer
 * through the {@link PaymentGateway} port. Both dependencies are
 * abstractions received through the constructor.
 */
public class PurchaseTicketUseCase {

    private final RaffleRepository raffleRepository;
    private final PaymentGateway paymentGateway;

    public PurchaseTicketUseCase(RaffleRepository raffleRepository, PaymentGateway paymentGateway) {
        this.raffleRepository = Objects.requireNonNull(raffleRepository, "RaffleRepository cannot be null");
        this.paymentGateway = Objects.requireNonNull(paymentGateway, "PaymentGateway cannot be null");
    }

    public boolean execute(String raffleId, Long ticketNumber, String userId) {
        if (raffleId == null || raffleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Raffle ID cannot be empty");
        }
        if (ticketNumber == null) {
            throw new IllegalArgumentException("Ticket number cannot be null");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }

        Raffle raffle = raffleRepository.findById(raffleId)
                .orElseThrow(() -> new RaffleNotFoundException("Raffle " + raffleId + " not found"));

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

        boolean paid = paymentGateway.processPayment(userId, ticket.getPrice().amount());
        if (!paid) {
            throw new PaymentFailedException("Payment failed for user " + userId + " on ticket " + ticketNumber);
        }

        ticket.assignToOwner(userId);
        raffleRepository.save(raffle);
        return true;
    }
}
