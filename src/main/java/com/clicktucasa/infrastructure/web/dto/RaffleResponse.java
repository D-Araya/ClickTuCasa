package com.clicktucasa.infrastructure.web.dto;

import com.clicktucasa.domain.entity.Raffle;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Full snapshot of a raffle, including all of its tickets")
public record RaffleResponse(

        @Schema(description = "Business identifier of the raffle", example = "raf-001")
        String id,

        @Schema(description = "Title of the raffle", example = "Luxury Villa Raffle")
        String title,

        @Schema(description = "Physical address of the house being raffled", example = "123 Ocean Drive")
        String houseAddress,

        @Schema(description = "Declared value of the house", example = "500000.00")
        BigDecimal houseValue,

        @Schema(description = "Minimum tickets that must be sold before the raffle can be drawn", example = "2")
        int minTicketsToDraw,

        @Schema(description = "Current lifecycle status of the raffle", example = "ACTIVE")
        String status,

        @Schema(description = "Winning ticket number, present only once the raffle has been DRAWN")
        Long winnerTicketNumber,

        @Schema(description = "Number of tickets still AVAILABLE", example = "7")
        long availableTickets,

        @Schema(description = "Number of tickets currently RESERVED", example = "1")
        long reservedTickets,

        @Schema(description = "Number of tickets already SOLD", example = "2")
        long soldTickets,

        @Schema(description = "Full list of tickets belonging to this raffle")
        List<TicketResponse> tickets
) {
    public static RaffleResponse from(Raffle raffle) {
        List<TicketResponse> ticketResponses = raffle.getTickets().stream()
                .map(TicketResponse::from)
                .toList();

        return new RaffleResponse(
                raffle.getId(),
                raffle.getTitle(),
                raffle.getHouseAddress().value(),
                raffle.getHouseValue().amount(),
                raffle.getMinTicketsToDraw(),
                raffle.getStatus().name(),
                raffle.getWinnerTicketNumber(),
                raffle.getAvailableTickets().size(),
                raffle.getReservedTickets().size(),
                raffle.getSoldTickets().size(),
                ticketResponses);
    }
}
