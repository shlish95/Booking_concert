package kr.hhplus.be.server.infrastructure.persistence.adapter;

import kr.hhplus.be.server.application.payment.port.out.PaymentPort;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertScheduleJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.PaymentJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.ReservationJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.SeatInventoryJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.PaymentMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.PaymentJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.ReservationJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.SeatInventoryJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Profile("!mock")
public class PaymentPersistenceAdapter implements PaymentPort {

    private final PaymentJpaRepository paymentJpaRepository;
    private final ReservationJpaRepository reservationJpaRepository;
    private final ConcertScheduleJpaRepository concertScheduleJpaRepository;
    private final ConcertJpaRepository concertJpaRepository;
    private final SeatInventoryJpaRepository seatInventoryJpaRepository;
    private final PaymentMapper paymentMapper;

    public PaymentPersistenceAdapter(
            PaymentJpaRepository paymentJpaRepository,
            ReservationJpaRepository reservationJpaRepository,
            ConcertScheduleJpaRepository concertScheduleJpaRepository,
            ConcertJpaRepository concertJpaRepository,
            SeatInventoryJpaRepository seatInventoryJpaRepository,
            PaymentMapper paymentMapper
    ) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.reservationJpaRepository = reservationJpaRepository;
        this.concertScheduleJpaRepository = concertScheduleJpaRepository;
        this.concertJpaRepository = concertJpaRepository;
        this.seatInventoryJpaRepository = seatInventoryJpaRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentContext getPaymentContext(Long userId, Long reservationId) {
        ReservationJpaEntity reservation = reservationJpaRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        ConcertScheduleJpaEntity schedule = concertScheduleJpaRepository.findById(reservation.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다."));

        ConcertJpaEntity concert = concertJpaRepository.findById(schedule.getConcertId())
                .orElseThrow(() -> new IllegalArgumentException("콘서트를 찾을 수 없습니다."));

        return new PaymentContext(
                reservation.getId(),
                schedule.getId(),
                concert.getSeatPrice(),
                schedule.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public Payment saveSuccess(Long userId, Long reservationId, Long amount, LocalDateTime paidAt) {
        ReservationJpaEntity reservation = reservationJpaRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        SeatInventoryJpaEntity seatInventory = seatInventoryJpaRepository.findByScheduleIdAndSeatNumber(
                        reservation.getScheduleId(),
                        reservation.getSeatNumber()
                )
                .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(paidAt);
        seatInventory.reserve(userId);

        PaymentJpaEntity payment = paymentJpaRepository.save(
                new PaymentJpaEntity(userId, reservationId, amount, PaymentStatus.SUCCESS, paidAt)
        );
        return paymentMapper.toDomain(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isScheduleSoldOut(Long scheduleId) {
        return seatInventoryJpaRepository.countByScheduleIdAndStatusNot(scheduleId, SeatStatus.RESERVED) == 0;
    }
}
