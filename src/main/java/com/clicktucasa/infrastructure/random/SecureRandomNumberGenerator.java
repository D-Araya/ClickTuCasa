package com.clicktucasa.infrastructure.random;

import com.clicktucasa.domain.port.RandomNumberGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Production adapter for the {@link RandomNumberGenerator} port, backed by
 * {@link SecureRandom} so that raffle draws (DrawWinnerUseCase) cannot be
 * predicted or biased.
 */
@Component
public class SecureRandomNumberGenerator implements RandomNumberGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public int generateRandomIndex(int maxBound) {
        return secureRandom.nextInt(maxBound);
    }
}
