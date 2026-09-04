package com.clicktucasa.infrastructure.web.dto;

import com.clicktucasa.domain.entity.Ticket;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of drawing the winner of a raffle")
public record DrawWinnerResponse(

        @Schema(description = "Winning ticket number", example = "7")
        Long winnerTicketNumber,

        @Schema(description = "Identifier of the winning user", example = "user-alice")
        String ownerId
) {
    public static DrawWinnerResponse from(Ticket winningTicket) {
        return new DrawWinnerResponse(winningTicket.getNumber(), winningTicket.getOwnerId());
    }
}
