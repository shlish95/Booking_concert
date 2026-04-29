package kr.hhplus.be.server.application.payment.port.out;

import kr.hhplus.be.server.domain.payment.Payment;

import java.time.LocalDateTime;

public interface PaymentPort {

    PaymentContext getPaymentContext(Long userId, Long reservationId);

    Payment saveSuccess(Long userId, Long reservationId, Long amount, LocalDateTime paidAt);

    boolean isScheduleSoldOut(Long scheduleId);

    record PaymentContext(
            Long reservationId,
            Long scheduleId,
            Long amount,
            LocalDateTime salesOpenedAt
    ) {
    }
}
