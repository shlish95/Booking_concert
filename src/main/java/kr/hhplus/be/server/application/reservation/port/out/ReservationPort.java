package kr.hhplus.be.server.application.reservation.port.out;

import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.seat.SeatStatus;

import java.time.LocalDateTime;

public interface ReservationPort {

    boolean hasActiveTemporaryReservation(Long userId, LocalDateTime now);

    LockedSeat getSeatForUpdate(Long scheduleId, Integer seatNumber);

    void holdSeat(Long scheduleId, Integer seatNumber, Long userId, LocalDateTime expiresAt);

    Reservation saveTemporaryReservation(
            Long userId,
            Long scheduleId,
            Integer seatNumber,
            LocalDateTime reservedAt,
            LocalDateTime expiresAt
    );

    record LockedSeat(
            Long scheduleId,
            Integer seatNumber,
            SeatStatus status,
            Long heldByUserId,
            LocalDateTime holdExpiresAt,
            Long reservedByUserId
    ) {
    }
}
