package com.clicktucasa.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload to purchase a raffle ticket outright")
public record PurchaseTicketRequest(

        @Schema(description = "Identifier of the user purchasing the ticket", example = "user-alice")
        @NotBlank(message = "userId cannot be empty")
        String userId
) {
}
