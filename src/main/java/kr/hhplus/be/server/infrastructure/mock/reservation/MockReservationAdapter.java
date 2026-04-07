package kr.hhplus.be.server.infrastructure.mock.reservation;

import kr.hhplus.be.server.application.reservation.port.out.ReservationPort;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("mock")
public class MockReservationAdapter implements ReservationPort {

    @Override
    public boolean hasActiveTemporaryReservation(Long userId, LocalDateTime now) {
        return false;
    }

    @Override
    public LockedSeat getSeatForUpdate(Long scheduleId, Integer seatNumber) {
        return new LockedSeat(
                scheduleId,
                seatNumber,
                SeatStatus.AVAILABLE,
                null,
                null,
                null
        );
    }

    @Override
    public void holdSeat(Long scheduleId, Integer seatNumber, Long userId, LocalDateTime expiresAt) {
    }

    @Override
    public Reservation saveTemporaryReservation(
            Long userId,
            Long scheduleId,
            Integer seatNumber,
            LocalDateTime reservedAt,
            LocalDateTime expiresAt
    ) {
        return new Reservation(
                5001L,
                userId,
                scheduleId,
                seatNumber,
                ReservationStatus.TEMPORARY,
                expiresAt
        );
    }
}
