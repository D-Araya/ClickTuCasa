package com.clicktucasa.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of releasing expired ticket reservations for a raffle")
public record ReleaseExpiredReservationsResponse(

        @Schema(description = "Number of reservations that were released back to AVAILABLE", example = "3")
        int releasedCount
) {
}
