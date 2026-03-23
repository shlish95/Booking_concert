package kr.hhplus.be.server.application.reservation.usecase;

import kr.hhplus.be.server.application.reservation.dto.ReservationResult;
import kr.hhplus.be.server.application.reservation.dto.ReserveSeatCommand;

public interface ReserveSeatUseCase {

    ReservationResult reserve(ReserveSeatCommand command);
}
