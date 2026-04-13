package kr.hhplus.be.server.application.payment.port.out;

import kr.hhplus.be.server.domain.payment.Payment;

import java.time.LocalDateTime;

public interface PaymentPort {

    Long getPaymentAmount(Long userId, Long reservationId);

    Payment saveSuccess(Long userId, Long reservationId, Long amount, LocalDateTime paidAt);
}
