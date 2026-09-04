package com.clicktucasa.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Payload to create a new raffle with a fixed pool of freshly minted tickets")
public record CreateRaffleRequest(

        @Schema(description = "Business identifier of the raffle", example = "raf-001")
        @NotBlank(message = "id cannot be empty")
        String id,

        @Schema(description = "Human-readable title of the raffle", example = "Luxury Villa Raffle")
        @NotBlank(message = "title cannot be empty")
        String title,

        @Schema(description = "Physical address of the house being raffled", example = "123 Ocean Drive")
        @NotBlank(message = "houseAddress cannot be empty")
        String houseAddress,

        @Schema(description = "Declared value of the house, must be positive", example = "500000.00")
        @NotNull(message = "houseValue is required")
        @Positive(message = "houseValue must be greater than zero")
        BigDecimal houseValue,

        @Schema(description = "Minimum number of tickets that must be sold before the raffle can be drawn", example = "2")
        @Positive(message = "minTicketsToDraw must be greater than zero")
        int minTicketsToDraw,

        @Schema(description = "Total number of tickets to mint for this raffle, numbered 1..N", example = "10")
        @Positive(message = "totalTickets must be greater than zero")
        int totalTickets,

        @Schema(description = "Price of each individual ticket, must be positive", example = "25.00")
        @NotNull(message = "ticketPrice is required")
        @Positive(message = "ticketPrice must be greater than zero")
        BigDecimal ticketPrice
) {
}
