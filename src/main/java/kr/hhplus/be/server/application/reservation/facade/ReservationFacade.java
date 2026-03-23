package kr.hhplus.be.server.application.reservation.facade;

import kr.hhplus.be.server.application.reservation.dto.ReservationResult;
import kr.hhplus.be.server.application.reservation.dto.ReserveSeatCommand;
import kr.hhplus.be.server.application.reservation.port.out.ReservationPort;
import kr.hhplus.be.server.application.reservation.usecase.ReserveSeatUseCase;
import kr.hhplus.be.server.domain.reservation.Reservation;
import org.springframework.stereotype.Service;

@Service
public class ReservationFacade implements ReserveSeatUseCase {

    private final ReservationPort reservationPort;

    public ReservationFacade(ReservationPort reservationPort) {
        this.reservationPort = reservationPort;
    }

    @Override
    public ReservationResult reserve(ReserveSeatCommand command) {
        Reservation reservation = reservationPort.reserve(
                command.queueToken(),
                command.userId(),
                command.scheduleId(),
                command.seatNumber()
        );

        return new ReservationResult(
                reservation.reservationId(),
                reservation.userId(),
                reservation.scheduleId(),
                reservation.seatNumber(),
                reservation.status().name(),
                reservation.expiresAt()
        );
    }
}
