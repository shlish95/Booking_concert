package kr.hhplus.be.server.application.payment.dto;

public record PayReservationCommand(
        String queueToken,
        Long userId,
        Long reservationId
) {
}
