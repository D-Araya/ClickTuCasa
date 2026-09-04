package com.clicktucasa.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Uniform error shape returned by every endpoint in this API — no
 * exception ever reaches the client as a raw stack trace (Pilar 1 of the
 * Hito 4 rubric). Produced exclusively by {@code GlobalExceptionHandler}.
 */
@Schema(description = "Uniform error payload returned by every failed request")
public record ErrorResponse(

        @Schema(description = "Human-readable explanation of what went wrong")
        String message,

        @Schema(description = "Machine-readable error category", example = "RAFFLE_NOT_FOUND")
        String errorCode,

        @Schema(description = "Moment the error was produced")
        LocalDateTime timestamp
) {
    public ErrorResponse(String message, String errorCode) {
        this(message, errorCode, LocalDateTime.now());
    }
}
