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
        validateRequest(command);
        planScheduleLookup(command);
        planQueueTokenValidation(command);
        planActiveReservationCheck(command);
        planSeatLookupAndValidation(command);
        planReservationExpiration(command);

        // TODO: facade가 회차 조회, 토큰 검증, 사용자 활성 임시 예약 확인,
        // 좌석 상태 검증, 만료 시각 계산, 좌석 hold 반영/예약 저장 순서를 직접 조합하도록 확장한다.
        Reservation reservation = reserveSeat(command);

        return new ReservationResult(
                reservation.reservationId(),
                reservation.userId(),
                reservation.scheduleId(),
                reservation.seatNumber(),
                reservation.status().name(),
                reservation.expiresAt()
        );
    }

    private void validateRequest(ReserveSeatCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("예약 요청은 null일 수 없습니다.");
        }
    }

    private void planScheduleLookup(ReserveSeatCommand command) {
        // TODO: scheduleId로 회차를 조회하고 예약 대상 회차 존재 여부를 검증한다.
    }

    private void planQueueTokenValidation(ReserveSeatCommand command) {
        // TODO: queueToken과 concertId 기준 토큰 조회/검증을 facade가 담당한다.
    }

    private void planActiveReservationCheck(ReserveSeatCommand command) {
        // TODO: 사용자 활성 임시 예약 존재 여부를 확인하는 흐름을 facade에 둔다.
    }

    private void planSeatLookupAndValidation(ReserveSeatCommand command) {
        // TODO: scheduleId + seatNumber 기준 좌석 조회와 상태 검증을 facade가 담당한다.
    }

    private void planReservationExpiration(ReserveSeatCommand command) {
        // TODO: 임시 배정 만료 시각 계산과 hold/예약 반영 순서를 facade에서 결정한다.
    }

    private Reservation reserveSeat(ReserveSeatCommand command) {
        return reservationPort.reserve(
                command.queueToken(),
                command.userId(),
                command.scheduleId(),
                command.seatNumber()
        );
    }
}
