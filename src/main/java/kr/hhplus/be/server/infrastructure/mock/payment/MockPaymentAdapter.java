package kr.hhplus.be.server.infrastructure.mock.payment;

import kr.hhplus.be.server.application.payment.port.out.PaymentPort;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("mock")
public class MockPaymentAdapter implements PaymentPort {

    @Override
    public Payment pay(String queueToken, Long userId, Long reservationId) {
        return new Payment(
                9001L,
                reservationId,
                userId,
                50_000L,
                PaymentStatus.SUCCESS,
                LocalDateTime.of(2026, 3, 12, 10, 2, 0)
        );
    }
}
