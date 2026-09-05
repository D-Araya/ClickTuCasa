package com.clicktucasa.infrastructure.web.dto;

import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.Ticket;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Lightweight projection of a raffle, used by the catalogue endpoint
 * ({@code GET /api/v1/raffles}).
 *
 * <p>Deliberately does <strong>not</strong> carry the ticket list. A raffle
 * can mint tens of thousands of tickets, so returning the full aggregate
 * for every raffle in the catalogue would push a payload three orders of
 * magnitude larger than the storefront actually needs to draw a card. The
 * complete graph stays available, one raffle at a time, through
 * {@code GET /api/v1/raffles/{raffleId}}.
 */
@Schema(description = "Summary of a raffle for catalogue listings, without the ticket list")
public record RaffleSummaryResponse(

        @Schema(description = "Business identifier of the raffle", example = "raf-001")
        String id,

        @Schema(description = "Title of the raffle", example = "Luxury Villa Raffle")
        String title,

        @Schema(description = "Physical address of the house being raffled", example = "123 Ocean Drive")
        String houseAddress,

        @Schema(description = "Declared value of the house", example = "500000.00")
        BigDecimal houseValue,

        @Schema(description = "Price of a single ticket in this raffle", example = "25.00")
        BigDecimal ticketPrice,

        @Schema(description = "Total number of tickets minted for this raffle", example = "10")
        int totalTickets,

        @Schema(description = "Minimum tickets that must be sold before the raffle can be drawn", example = "2")
        int minTicketsToDraw,

        @Schema(description = "Current lifecycle status of the raffle", example = "ACTIVE")
        String status,

        @Schema(description = "Winning ticket number, present only once the raffle has been DRAWN")
        Long winnerTicketNumber,

        @Schema(description = "Number of tickets still AVAILABLE", example = "7")
        int availableTickets,

        @Schema(description = "Number of tickets currently RESERVED", example = "1")
        int reservedTickets,

        @Schema(description = "Number of tickets already SOLD", example = "2")
        int soldTickets
) {
    public static RaffleSummaryResponse from(Raffle raffle) {
        return new RaffleSummaryResponse(
                raffle.getId(),
                raffle.getTitle(),
                raffle.getHouseAddress().value(),
                raffle.getHouseValue().amount(),
                resolveTicketPrice(raffle),
                raffle.getTickets().size(),
                raffle.getMinTicketsToDraw(),
                raffle.getStatus().name(),
                raffle.getWinnerTicketNumber(),
                raffle.getAvailableTickets().size(),
                raffle.getReservedTickets().size(),
                raffle.getSoldTickets().size());
    }

    /**
     * Every ticket in a raffle is minted with the same price, so the first
     * one is representative. A raffle with no tickets cannot exist through
     * the domain (CreateRaffleUseCase requires a positive pool), but the
     * fallback keeps the DTO total rather than throwing on malformed data.
     */
    private static BigDecimal resolveTicketPrice(Raffle raffle) {
        return raffle.getTickets().stream()
                .findFirst()
                .map(Ticket::getPrice)
                .map(price -> price.amount())
                .orElse(BigDecimal.ZERO);
    }
}
