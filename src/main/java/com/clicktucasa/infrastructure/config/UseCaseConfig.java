package com.clicktucasa.infrastructure.config;

import com.clicktucasa.application.usecase.CancelRaffleUseCase;
import com.clicktucasa.application.usecase.CreateRaffleUseCase;
import com.clicktucasa.application.usecase.DrawWinnerUseCase;
import com.clicktucasa.application.usecase.GetRaffleUseCase;
import com.clicktucasa.application.usecase.ListRafflesUseCase;
import com.clicktucasa.application.usecase.PurchaseTicketUseCase;
import com.clicktucasa.application.usecase.ReleaseExpiredReservationsUseCase;
import com.clicktucasa.application.usecase.ReserveTicketUseCase;
import com.clicktucasa.domain.port.PaymentGateway;
import com.clicktucasa.domain.port.RandomNumberGenerator;
import com.clicktucasa.domain.repository.RaffleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires every application-layer use case with its constructor
 * dependencies. Kept as a dedicated {@code @Configuration} class —
 * instead of annotating the use cases themselves with {@code @Service} —
 * so that {@code application.usecase} stays exactly as framework-agnostic
 * as {@code domain}: no Spring annotation appears anywhere outside
 * {@code infrastructure}, and every use case is still unit-testable with
 * plain Mockito, no Spring context required.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public CreateRaffleUseCase createRaffleUseCase(RaffleRepository raffleRepository) {
        return new CreateRaffleUseCase(raffleRepository);
    }

    @Bean
    public GetRaffleUseCase getRaffleUseCase(RaffleRepository raffleRepository) {
        return new GetRaffleUseCase(raffleRepository);
    }

    @Bean
    public ListRafflesUseCase listRafflesUseCase(RaffleRepository raffleRepository) {
        return new ListRafflesUseCase(raffleRepository);
    }

    @Bean
    public ReserveTicketUseCase reserveTicketUseCase(RaffleRepository raffleRepository) {
        return new ReserveTicketUseCase(raffleRepository);
    }

    @Bean
    public PurchaseTicketUseCase purchaseTicketUseCase(RaffleRepository raffleRepository, PaymentGateway paymentGateway) {
        return new PurchaseTicketUseCase(raffleRepository, paymentGateway);
    }

    @Bean
    public DrawWinnerUseCase drawWinnerUseCase(RaffleRepository raffleRepository, RandomNumberGenerator randomNumberGenerator) {
        return new DrawWinnerUseCase(raffleRepository, randomNumberGenerator);
    }

    @Bean
    public ReleaseExpiredReservationsUseCase releaseExpiredReservationsUseCase(RaffleRepository raffleRepository) {
        return new ReleaseExpiredReservationsUseCase(raffleRepository);
    }

    @Bean
    public CancelRaffleUseCase cancelRaffleUseCase(RaffleRepository raffleRepository) {
        return new CancelRaffleUseCase(raffleRepository);
    }
}
