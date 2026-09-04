package com.clicktucasa.infrastructure.web.dto;

import com.clicktucasa.domain.entity.Ticket;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Snapshot of a single raffle ticket")
public record TicketResponse(

        @Schema(description = "Ticket number within its raffle", example = "1")
        Long number,

        @Schema(description = "Ticket price", example = "25.00")
        BigDecimal price,

        @Schema(description = "Current lifecycle status of the ticket", example = "AVAILABLE")
        String status,

        @Schema(description = "Identifier of the user who reserved or bought the ticket, if any", example = "user-alice")
        String ownerId,

        @Schema(description = "Moment the reservation expires, if the ticket is currently RESERVED")
        LocalDateTime reservedUntil
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getNumber(),
                ticket.getPrice().amount(),
                ticket.getStatus().name(),
                ticket.getOwnerId(),
                ticket.getReservedUntil());
    }
}
