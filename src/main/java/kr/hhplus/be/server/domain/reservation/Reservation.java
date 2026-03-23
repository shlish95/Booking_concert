package kr.hhplus.be.server.domain.reservation;

import java.time.LocalDateTime;

public record Reservation(
        Long reservationId,
        Long userId,
        Long scheduleId,
        Integer seatNumber,
        ReservationStatus status,
        LocalDateTime expiresAt
) {
}
