package kr.hhplus.be.server.application.payment.dto;

import java.time.LocalDateTime;

public record PaymentResult(
        Long paymentId,
        Long reservationId,
        Long userId,
        Long amount,
        String status,
        LocalDateTime paidAt
) {
}
