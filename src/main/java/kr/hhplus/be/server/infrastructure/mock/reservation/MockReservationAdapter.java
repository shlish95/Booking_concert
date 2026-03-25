package kr.hhplus.be.server.infrastructure.mock.reservation;

import kr.hhplus.be.server.application.reservation.port.out.ReservationPort;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("mock")
public class MockReservationAdapter implements ReservationPort {

    @Override
    public Reservation reserve(String queueToken, Long userId, Long scheduleId, Integer seatNumber) {
        return new Reservation(
                5001L,
                userId,
                scheduleId,
                seatNumber,
                ReservationStatus.TEMPORARY,
                LocalDateTime.of(2026, 3, 12, 10, 5, 0)
        );
    }
}
