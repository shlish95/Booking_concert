package kr.hhplus.be.server.application.reservation.port.out;

import kr.hhplus.be.server.domain.reservation.Reservation;

public interface ReservationPort {

    Reservation reserve(String queueToken, Long userId, Long scheduleId, Integer seatNumber);
}
