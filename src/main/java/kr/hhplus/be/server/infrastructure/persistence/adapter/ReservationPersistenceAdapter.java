package kr.hhplus.be.server.infrastructure.persistence.adapter;

import kr.hhplus.be.server.application.reservation.port.out.ReservationPort;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.infrastructure.persistence.entity.ReservationJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.SeatInventoryJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.ReservationMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.ReservationJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.SeatInventoryJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Profile("!mock")
public class ReservationPersistenceAdapter implements ReservationPort {

    private final ReservationJpaRepository reservationJpaRepository;
    private final SeatInventoryJpaRepository seatInventoryJpaRepository;
    private final ReservationMapper reservationMapper;

    public ReservationPersistenceAdapter(
            ReservationJpaRepository reservationJpaRepository,
            SeatInventoryJpaRepository seatInventoryJpaRepository,
            ReservationMapper reservationMapper
    ) {
        this.reservationJpaRepository = reservationJpaRepository;
        this.seatInventoryJpaRepository = seatInventoryJpaRepository;
        this.reservationMapper = reservationMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveTemporaryReservation(Long userId, LocalDateTime now) {
        return reservationJpaRepository.existsByUserIdAndStatusAndExpiresAtAfter(
                userId,
                ReservationStatus.TEMPORARY,
                now
        );
    }

    @Override
    @Transactional
    public LockedSeat getSeatForUpdate(Long scheduleId, Integer seatNumber) {
        SeatInventoryJpaEntity seatInventory = seatInventoryJpaRepository.findByScheduleIdAndSeatNumberForUpdate(
                        scheduleId,
                        seatNumber
                )
                .orElseThrow(() -> new IllegalArgumentException("좌석 정보를 찾을 수 없습니다."));

        return new LockedSeat(
                seatInventory.getScheduleId(),
                seatInventory.getSeatNumber(),
                seatInventory.getStatus(),
                seatInventory.getHeldByUserId(),
                seatInventory.getHoldExpiresAt(),
                seatInventory.getReservedByUserId()
        );
    }

    @Override
    @Transactional
    public void holdSeat(Long scheduleId, Integer seatNumber, Long userId, LocalDateTime expiresAt) {
        SeatInventoryJpaEntity seatInventory = seatInventoryJpaRepository.findByScheduleIdAndSeatNumber(
                        scheduleId,
                        seatNumber
                )
                .orElseThrow(() -> new IllegalArgumentException("좌석 정보를 찾을 수 없습니다."));

        seatInventory.hold(userId, expiresAt);
    }

    @Override
    @Transactional
    public Reservation saveTemporaryReservation(
            Long userId,
            Long scheduleId,
            Integer seatNumber,
            LocalDateTime reservedAt,
            LocalDateTime expiresAt
    ) {
        ReservationJpaEntity reservation = new ReservationJpaEntity(
                userId,
                scheduleId,
                seatNumber,
                ReservationStatus.TEMPORARY,
                reservedAt,
                expiresAt,
                null
        );

        return reservationMapper.toDomain(reservationJpaRepository.save(reservation));
    }
}
