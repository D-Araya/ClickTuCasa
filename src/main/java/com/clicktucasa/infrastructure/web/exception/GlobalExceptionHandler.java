package com.clicktucasa.infrastructure.web.exception;

import com.clicktucasa.domain.exception.InvalidHouseAddressException;
import com.clicktucasa.domain.exception.InvalidHouseValueException;
import com.clicktucasa.domain.exception.InvalidRaffleOperationException;
import com.clicktucasa.domain.exception.InvalidTicketPriceException;
import com.clicktucasa.domain.exception.PaymentFailedException;
import com.clicktucasa.domain.exception.RaffleNotFoundException;
import com.clicktucasa.domain.exception.TicketNotAvailableException;
import com.clicktucasa.domain.exception.TicketNotFoundException;
import com.clicktucasa.infrastructure.web.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Single perimeter that every controller in this API funnels its
 * exceptions through (Pilar 1 of the Hito 4 rubric): no matter which
 * business rule or validation constraint is violated, the client always
 * receives the same {@link ErrorResponse} shape with a semantic HTTP
 * status code — never a raw Spring/Java stack trace.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({RaffleNotFoundException.class, TicketNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), "RESOURCE_NOT_FOUND");
    }

    @ExceptionHandler({InvalidRaffleOperationException.class, TicketNotAvailableException.class})
    public ResponseEntity<ErrorResponse> handleBusinessConflict(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), "BUSINESS_RULE_VIOLATION");
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentFailed(PaymentFailedException ex) {
        return build(HttpStatus.PAYMENT_REQUIRED, ex.getMessage(), "PAYMENT_FAILED");
    }

    @ExceptionHandler({InvalidHouseAddressException.class, InvalidHouseValueException.class,
            InvalidTicketPriceException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleInvalidInput(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_INPUT");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, "VALIDATION_ERROR");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "INTERNAL_ERROR");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String errorCode) {
        return new ResponseEntity<>(new ErrorResponse(message, errorCode), status);
    }
}
