package kr.hhplus.be.server.domain.payment;

import java.time.LocalDateTime;

public record Payment(
        Long paymentId,
        Long reservationId,
        Long userId,
        Long amount,
        PaymentStatus status,
        LocalDateTime paidAt
) {
}
