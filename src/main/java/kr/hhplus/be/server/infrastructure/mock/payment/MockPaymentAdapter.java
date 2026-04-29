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
    public PaymentContext getPaymentContext(Long userId, Long reservationId) {
        return new PaymentContext(
                reservationId,
                1L,
                50_000L,
                LocalDateTime.now().minusMinutes(10)
        );
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

    @Override
    public boolean isScheduleSoldOut(Long scheduleId) {
        return false;
    }
}
