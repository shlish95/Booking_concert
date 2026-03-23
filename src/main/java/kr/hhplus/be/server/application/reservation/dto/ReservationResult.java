package kr.hhplus.be.server.application.reservation.dto;

import java.time.LocalDateTime;

public record ReservationResult(
        Long reservationId,
        Long userId,
        Long scheduleId,
        Integer seatNumber,
        String status,
        LocalDateTime expiresAt
) {
}
