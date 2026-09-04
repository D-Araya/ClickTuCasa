package com.clicktucasa.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "Payload to temporarily reserve a raffle ticket for a user")
public record ReserveTicketRequest(

        @Schema(description = "Identifier of the user reserving the ticket", example = "user-alice")
        @NotBlank(message = "userId cannot be empty")
        String userId,

        @Schema(description = "How many minutes the reservation stays valid before it expires", example = "15")
        @Positive(message = "durationMinutes must be greater than zero")
        int durationMinutes
) {
}
