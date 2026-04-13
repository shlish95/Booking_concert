package kr.hhplus.be.server.infrastructure.persistence.adapter;

import kr.hhplus.be.server.application.payment.port.out.PaymentPort;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertScheduleJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.PaymentJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.ReservationJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.PaymentMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.PaymentJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.ReservationJpaRepository;
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
    private final PaymentMapper paymentMapper;

    public PaymentPersistenceAdapter(
            PaymentJpaRepository paymentJpaRepository,
            ReservationJpaRepository reservationJpaRepository,
            ConcertScheduleJpaRepository concertScheduleJpaRepository,
            ConcertJpaRepository concertJpaRepository,
            PaymentMapper paymentMapper
    ) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.reservationJpaRepository = reservationJpaRepository;
        this.concertScheduleJpaRepository = concertScheduleJpaRepository;
        this.concertJpaRepository = concertJpaRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPaymentAmount(Long userId, Long reservationId) {
        ReservationJpaEntity reservation = reservationJpaRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        ConcertScheduleJpaEntity schedule = concertScheduleJpaRepository.findById(reservation.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다."));

        ConcertJpaEntity concert = concertJpaRepository.findById(schedule.getConcertId())
                .orElseThrow(() -> new IllegalArgumentException("콘서트를 찾을 수 없습니다."));

        return concert.getSeatPrice();
    }

    @Override
    @Transactional
    public Payment saveSuccess(Long userId, Long reservationId, Long amount, LocalDateTime paidAt) {
        PaymentJpaEntity payment = paymentJpaRepository.save(
                new PaymentJpaEntity(userId, reservationId, amount, PaymentStatus.SUCCESS, paidAt)
        );
        return paymentMapper.toDomain(payment);
    }
}
