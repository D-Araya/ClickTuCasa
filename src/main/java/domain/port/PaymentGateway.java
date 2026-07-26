package domain.port;

import java.math.BigDecimal;

public interface PaymentGateway {
    boolean processPayment(String userId, BigDecimal amount);
}
