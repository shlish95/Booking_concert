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
    public Long getPaymentAmount(Long userId, Long reservationId) {
        return 50_000L;
    }

    @Override
    public Payment saveSuccess(Long userId, Long reservationId, Long amount, LocalDateTime paidAt) {
        return new Payment(
                9001L,
                reservationId,
                userId,
                amount,
                PaymentStatus.SUCCESS,
                paidAt
        );
    }
}
