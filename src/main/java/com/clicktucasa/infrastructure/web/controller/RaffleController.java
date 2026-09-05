package com.clicktucasa.infrastructure.web.controller;

import com.clicktucasa.application.usecase.CancelRaffleUseCase;
import com.clicktucasa.application.usecase.CreateRaffleUseCase;
import com.clicktucasa.application.usecase.DrawWinnerUseCase;
import com.clicktucasa.application.usecase.GetRaffleUseCase;
import com.clicktucasa.application.usecase.ListRafflesUseCase;
import com.clicktucasa.application.usecase.PurchaseTicketUseCase;
import com.clicktucasa.application.usecase.ReleaseExpiredReservationsUseCase;
import com.clicktucasa.application.usecase.ReserveTicketUseCase;
import com.clicktucasa.domain.entity.Raffle;
import com.clicktucasa.domain.entity.Ticket;
import com.clicktucasa.domain.valueobject.HouseAddress;
import com.clicktucasa.domain.valueobject.HouseValue;
import com.clicktucasa.domain.valueobject.TicketPrice;
import com.clicktucasa.infrastructure.web.dto.CreateRaffleRequest;
import com.clicktucasa.infrastructure.web.dto.DrawWinnerResponse;
import com.clicktucasa.infrastructure.web.dto.ErrorResponse;
import com.clicktucasa.infrastructure.web.dto.PurchaseTicketRequest;
import com.clicktucasa.infrastructure.web.dto.RaffleResponse;
import com.clicktucasa.infrastructure.web.dto.RaffleSummaryResponse;
import com.clicktucasa.infrastructure.web.dto.ReleaseExpiredReservationsResponse;
import com.clicktucasa.infrastructure.web.dto.ReserveTicketRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST entry point for the raffle domain (Pilar 1 of the Hito 4 rubric):
 * semantic routes under {@code /api/v1/raffles}, one HTTP verb per
 * intent, and every use case invoked strictly through its
 * application-layer interface — this class never touches
 * {@code RaffleRepository} or any persistence detail directly.
 */
@RestController
@RequestMapping("/api/v1/raffles")
@Tag(name = "Raffles", description = "Create, consult and run the full lifecycle of house raffles")
public class RaffleController {

    private final CreateRaffleUseCase createRaffleUseCase;
    private final GetRaffleUseCase getRaffleUseCase;
    private final ListRafflesUseCase listRafflesUseCase;
    private final ReserveTicketUseCase reserveTicketUseCase;
    private final PurchaseTicketUseCase purchaseTicketUseCase;
    private final DrawWinnerUseCase drawWinnerUseCase;
    private final ReleaseExpiredReservationsUseCase releaseExpiredReservationsUseCase;
    private final CancelRaffleUseCase cancelRaffleUseCase;

    public RaffleController(CreateRaffleUseCase createRaffleUseCase,
                             GetRaffleUseCase getRaffleUseCase,
                             ListRafflesUseCase listRafflesUseCase,
                             ReserveTicketUseCase reserveTicketUseCase,
                             PurchaseTicketUseCase purchaseTicketUseCase,
                             DrawWinnerUseCase drawWinnerUseCase,
                             ReleaseExpiredReservationsUseCase releaseExpiredReservationsUseCase,
                             CancelRaffleUseCase cancelRaffleUseCase) {
        this.createRaffleUseCase = createRaffleUseCase;
        this.getRaffleUseCase = getRaffleUseCase;
        this.listRafflesUseCase = listRafflesUseCase;
        this.reserveTicketUseCase = reserveTicketUseCase;
        this.purchaseTicketUseCase = purchaseTicketUseCase;
        this.drawWinnerUseCase = drawWinnerUseCase;
        this.releaseExpiredReservationsUseCase = releaseExpiredReservationsUseCase;
        this.cancelRaffleUseCase = cancelRaffleUseCase;
    }

    @GetMapping
    @Operation(summary = "List every raffle",
            description = "Returns the whole catalogue as lightweight summaries. The ticket list is omitted on purpose: "
                    + "fetch a single raffle to get its full ticket grid.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalogue returned, possibly empty",
                    content = @Content(schema = @Schema(implementation = RaffleSummaryResponse.class)))
    })
    public ResponseEntity<List<RaffleSummaryResponse>> listAll() {
        List<RaffleSummaryResponse> catalogue = listRafflesUseCase.execute().stream()
                .map(RaffleSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(catalogue);
    }

    @PostMapping
    @Operation(summary = "Create a raffle", description = "Creates a raffle with a fixed pool of freshly minted AVAILABLE tickets")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Raffle created",
                    content = @Content(schema = @Schema(implementation = RaffleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RaffleResponse> create(@Valid @RequestBody CreateRaffleRequest request) {
        Raffle raffle = createRaffleUseCase.execute(
                request.id(),
                request.title(),
                new HouseAddress(request.houseAddress()),
                new HouseValue(request.houseValue()),
                request.minTicketsToDraw(),
                request.totalTickets(),
                new TicketPrice(request.ticketPrice()));
        return ResponseEntity.status(HttpStatus.CREATED).body(RaffleResponse.from(raffle));
    }

    @GetMapping("/{raffleId}")
    @Operation(summary = "Get a raffle by id", description = "Returns the full current state of a raffle, including every ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Raffle found",
                    content = @Content(schema = @Schema(implementation = RaffleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Raffle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RaffleResponse> getById(@PathVariable String raffleId) {
        Raffle raffle = getRaffleUseCase.execute(raffleId);
        return ResponseEntity.ok(RaffleResponse.from(raffle));
    }

    @PostMapping("/{raffleId}/tickets/{ticketNumber}/reservations")
    @Operation(summary = "Reserve a ticket", description = "Temporarily reserves an AVAILABLE ticket for a user for a limited time window")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket reserved",
                    content = @Content(schema = @Schema(implementation = RaffleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Raffle or ticket not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ticket is not AVAILABLE, or raffle is not ACTIVE",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RaffleResponse> reserveTicket(@PathVariable String raffleId,
                                                         @PathVariable Long ticketNumber,
                                                         @Valid @RequestBody ReserveTicketRequest request) {
        reserveTicketUseCase.execute(raffleId, ticketNumber, request.userId(), request.durationMinutes(), LocalDateTime.now());
        return ResponseEntity.ok(RaffleResponse.from(getRaffleUseCase.execute(raffleId)));
    }

    @PostMapping("/{raffleId}/tickets/{ticketNumber}/purchases")
    @Operation(summary = "Purchase a ticket", description = "Buys a ticket outright, charging the buyer through the payment gateway")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket purchased",
                    content = @Content(schema = @Schema(implementation = RaffleResponse.class))),
            @ApiResponse(responseCode = "402", description = "Payment failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Raffle or ticket not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ticket is SOLD, or reserved by another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RaffleResponse> purchaseTicket(@PathVariable String raffleId,
                                                          @PathVariable Long ticketNumber,
                                                          @Valid @RequestBody PurchaseTicketRequest request) {
        purchaseTicketUseCase.execute(raffleId, ticketNumber, request.userId());
        return ResponseEntity.ok(RaffleResponse.from(getRaffleUseCase.execute(raffleId)));
    }

    @PostMapping("/{raffleId}/draw")
    @Operation(summary = "Draw the raffle winner", description = "Randomly selects the winning ticket among the SOLD tickets, once the minimum has been reached")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Winner drawn",
                    content = @Content(schema = @Schema(implementation = DrawWinnerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Raffle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Raffle cannot be drawn yet",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DrawWinnerResponse> drawWinner(@PathVariable String raffleId) {
        Ticket winningTicket = drawWinnerUseCase.execute(raffleId);
        return ResponseEntity.ok(DrawWinnerResponse.from(winningTicket));
    }

    @PostMapping("/{raffleId}/expired-reservations/release")
    @Operation(summary = "Release expired reservations", description = "Frees up every RESERVED ticket whose reservation window has expired")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expired reservations released",
                    content = @Content(schema = @Schema(implementation = ReleaseExpiredReservationsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Raffle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ReleaseExpiredReservationsResponse> releaseExpiredReservations(@PathVariable String raffleId) {
        int releasedCount = releaseExpiredReservationsUseCase.execute(raffleId, LocalDateTime.now());
        return ResponseEntity.ok(new ReleaseExpiredReservationsResponse(releasedCount));
    }

    @DeleteMapping("/{raffleId}")
    @Operation(summary = "Cancel a raffle", description = "Cancels a raffle that has not been drawn yet")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Raffle cancelled"),
            @ApiResponse(responseCode = "404", description = "Raffle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Raffle has already been DRAWN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> cancelRaffle(@PathVariable String raffleId) {
        cancelRaffleUseCase.execute(raffleId);
        return ResponseEntity.noContent().build();
    }
}
