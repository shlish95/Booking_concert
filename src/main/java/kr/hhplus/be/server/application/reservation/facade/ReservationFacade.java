package kr.hhplus.be.server.application.reservation.facade;

import kr.hhplus.be.server.application.concert.port.out.ConcertQueryPort;
import kr.hhplus.be.server.application.reservation.dto.ReservationResult;
import kr.hhplus.be.server.application.reservation.dto.ReserveSeatCommand;
import kr.hhplus.be.server.application.reservation.port.out.ReservationPort;
import kr.hhplus.be.server.application.reservation.usecase.ReserveSeatUseCase;
import kr.hhplus.be.server.application.queue.port.out.QueueTokenPort;
import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenNotActiveException;
import kr.hhplus.be.server.domain.queue.QueueTokenStatus;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.UserAlreadyHasHeldSeatException;
import kr.hhplus.be.server.domain.seat.SeatAlreadyHeldException;
import kr.hhplus.be.server.domain.seat.SeatAlreadyReservedException;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReservationFacade implements ReserveSeatUseCase {

    private static final long HOLD_MINUTES = 5L;

    private final ConcertQueryPort concertQueryPort;
    private final QueueTokenPort queueTokenPort;
    private final ReservationPort reservationPort;

    public ReservationFacade(
            ConcertQueryPort concertQueryPort,
            QueueTokenPort queueTokenPort,
            ReservationPort reservationPort
    ) {
        this.concertQueryPort = concertQueryPort;
        this.queueTokenPort = queueTokenPort;
        this.reservationPort = reservationPort;
    }

    @Override
    @Transactional
    public ReservationResult reserve(ReserveSeatCommand command) {
        validateRequest(command);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = calculateTemporaryHoldExpiration(now);

        ConcertSchedule schedule = getSchedule(command.scheduleId());
        QueueToken queueToken = getAndValidateQueueToken(schedule.concertId(), command.queueToken());
        validateNoActiveTemporaryReservation(command.userId(), now);
        ReservationPort.LockedSeat lockedSeat = getLockedSeat(command.scheduleId(), command.seatNumber());
        validateSeatAvailability(lockedSeat, now);

        Reservation reservation = holdAndSaveTemporaryReservation(
                command,
                now,
                expiresAt,
                queueToken,
                lockedSeat
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

    private void validateRequest(ReserveSeatCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("예약 요청은 null일 수 없습니다.");
        }
    }

    private ConcertSchedule getSchedule(Long scheduleId) {
        return concertQueryPort.getSchedule(scheduleId);
    }

    private QueueToken getAndValidateQueueToken(Long concertId, String token) {
        QueueToken queueToken = queueTokenPort.findByConcertIdAndToken(concertId, token);
        if (queueToken.status() != QueueTokenStatus.ACTIVE) {
            throw new QueueTokenNotActiveException();
        }
        return queueToken;
    }

    private void validateNoActiveTemporaryReservation(Long userId, LocalDateTime now) {
        if (reservationPort.hasActiveTemporaryReservation(userId, now)) {
            throw new UserAlreadyHasHeldSeatException();
        }
    }

    private ReservationPort.LockedSeat getLockedSeat(Long scheduleId, Integer seatNumber) {
        return reservationPort.getSeatForUpdate(scheduleId, seatNumber);
    }

    private void validateSeatAvailability(ReservationPort.LockedSeat seat, LocalDateTime now) {
        if (seat.status() == SeatStatus.RESERVED) {
            throw new SeatAlreadyReservedException();
        }

        if (seat.status() == SeatStatus.HELD
                && seat.holdExpiresAt() != null
                && seat.holdExpiresAt().isAfter(now)) {
            throw new SeatAlreadyHeldException();
        }
    }

    private LocalDateTime calculateTemporaryHoldExpiration(LocalDateTime now) {
        return now.plusMinutes(HOLD_MINUTES);
    }

    private Reservation holdAndSaveTemporaryReservation(
            ReserveSeatCommand command,
            LocalDateTime now,
            LocalDateTime expiresAt,
            QueueToken queueToken,
            ReservationPort.LockedSeat lockedSeat
    ) {
        // TODO: 이후 단계에서 queueToken/lockedSeat 기반 로그 포인트와 추가 검증을 보강한다.
        reservationPort.holdSeat(
                lockedSeat.scheduleId(),
                lockedSeat.seatNumber(),
                command.userId(),
                expiresAt
        );

        return reservationPort.saveTemporaryReservation(
                command.userId(),
                command.scheduleId(),
                command.seatNumber(),
                now,
                expiresAt
        );
    }
}
