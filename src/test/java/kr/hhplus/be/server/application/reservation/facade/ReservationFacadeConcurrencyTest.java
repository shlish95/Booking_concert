package kr.hhplus.be.server.application.reservation.facade;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.application.reservation.dto.ReserveSeatCommand;
import kr.hhplus.be.server.domain.queue.QueueTokenNotActiveException;
import kr.hhplus.be.server.domain.queue.QueueTokenStatus;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.reservation.UserAlreadyHasHeldSeatException;
import kr.hhplus.be.server.domain.seat.SeatAlreadyHeldException;
import kr.hhplus.be.server.domain.seat.SeatAlreadyReservedException;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.infrastructure.persistence.adapter.ConcertQueryPersistenceAdapter;
import kr.hhplus.be.server.infrastructure.persistence.adapter.QueueTokenPersistenceAdapter;
import kr.hhplus.be.server.infrastructure.persistence.adapter.ReservationPersistenceAdapter;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertScheduleJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.QueueTokenJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.ReservationJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.SeatInventoryJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.ConcertScheduleMapper;
import kr.hhplus.be.server.infrastructure.persistence.mapper.QueueTokenMapper;
import kr.hhplus.be.server.infrastructure.persistence.mapper.ReservationMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.QueueTokenJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.ReservationJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.SeatInventoryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({
        TestcontainersConfiguration.class,
        ReservationFacade.class,
        ConcertQueryPersistenceAdapter.class,
        QueueTokenPersistenceAdapter.class,
        ReservationPersistenceAdapter.class,
        ConcertScheduleMapper.class,
        QueueTokenMapper.class,
        ReservationMapper.class
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationFacadeConcurrencyTest {

    private static final Long CONCERT_ID = 1L;

    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private ConcertScheduleJpaRepository concertScheduleJpaRepository;

    @Autowired
    private QueueTokenJpaRepository queueTokenJpaRepository;

    @Autowired
    private SeatInventoryJpaRepository seatInventoryJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @BeforeEach
    void setUp() {
        reservationJpaRepository.deleteAll();
        seatInventoryJpaRepository.deleteAll();
        queueTokenJpaRepository.deleteAll();
        concertScheduleJpaRepository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("동일 좌석에 대한 동시 예약 요청은 한 건만 성공한다")
    void reserveSameSeatConcurrently_onlyOneSucceeds() throws Exception {
        Long scheduleId = createSchedule(CONCERT_ID, LocalDate.of(2026, 4, 10));

        seatInventoryJpaRepository.save(
                new SeatInventoryJpaEntity(scheduleId, 7, SeatStatus.AVAILABLE, null, null, null)
        );

        queueTokenJpaRepository.saveAll(List.of(
                createQueueToken("qt-1", 101L, CONCERT_ID, 1L, QueueTokenStatus.ACTIVE),
                createQueueToken("qt-2", 102L, CONCERT_ID, 2L, QueueTokenStatus.ACTIVE)
        ));

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Future<Boolean> future1 = executorService.submit(tryReserve(startLatch, new ReserveSeatCommand("qt-1", 101L, scheduleId, 7)));
        Future<Boolean> future2 = executorService.submit(tryReserve(startLatch, new ReserveSeatCommand("qt-2", 102L, scheduleId, 7)));

        startLatch.countDown();

        int successCount = 0;
        successCount += future1.get() ? 1 : 0;
        successCount += future2.get() ? 1 : 0;

        executorService.shutdown();

        assertThat(successCount).isEqualTo(1);
        assertThat(reservationJpaRepository.count()).isEqualTo(1);
        assertThat(seatInventoryJpaRepository.findByScheduleIdAndSeatNumber(scheduleId, 7))
                .isPresent()
                .get()
                .satisfies(seat -> {
                    assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
                    assertThat(seat.getHeldByUserId()).isIn(101L, 102L);
                    assertThat(seat.getHoldExpiresAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("유효한 HELD 좌석은 재선점이 실패해야 한다")
    void reserveHeldSeatBeforeExpiration_fails() {
        Long scheduleId = createSchedule(CONCERT_ID, LocalDate.of(2026, 4, 11));
        LocalDateTime futureExpiration = LocalDateTime.now().plusMinutes(3);

        seatInventoryJpaRepository.save(
                new SeatInventoryJpaEntity(scheduleId, 11, SeatStatus.HELD, 999L, futureExpiration, null)
        );
        queueTokenJpaRepository.save(createQueueToken("qt-held", 101L, CONCERT_ID, 1L, QueueTokenStatus.ACTIVE));

        assertThatThrownBy(() -> reservationFacade.reserve(new ReserveSeatCommand("qt-held", 101L, scheduleId, 11)))
                .isInstanceOf(SeatAlreadyHeldException.class);

        assertThat(reservationJpaRepository.count()).isZero();
        assertThat(seatInventoryJpaRepository.findByScheduleIdAndSeatNumber(scheduleId, 11))
                .isPresent()
                .get()
                .satisfies(seat -> {
                    assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
                    assertThat(seat.getHeldByUserId()).isEqualTo(999L);
                    assertThat(seat.getHoldExpiresAt()).isEqualTo(futureExpiration);
                });
    }

    @Test
    @DisplayName("RESERVED 좌석은 재선점이 실패해야 한다")
    void reserveReservedSeat_fails() {
        Long scheduleId = createSchedule(CONCERT_ID, LocalDate.of(2026, 4, 12));

        seatInventoryJpaRepository.save(
                new SeatInventoryJpaEntity(scheduleId, 12, SeatStatus.RESERVED, null, null, 777L)
        );
        queueTokenJpaRepository.save(createQueueToken("qt-reserved", 101L, CONCERT_ID, 1L, QueueTokenStatus.ACTIVE));

        assertThatThrownBy(() -> reservationFacade.reserve(new ReserveSeatCommand("qt-reserved", 101L, scheduleId, 12)))
                .isInstanceOf(SeatAlreadyReservedException.class);

        assertThat(reservationJpaRepository.count()).isZero();
        assertThat(seatInventoryJpaRepository.findByScheduleIdAndSeatNumber(scheduleId, 12))
                .isPresent()
                .get()
                .satisfies(seat -> {
                    assertThat(seat.getStatus()).isEqualTo(SeatStatus.RESERVED);
                    assertThat(seat.getReservedByUserId()).isEqualTo(777L);
                    assertThat(seat.getHeldByUserId()).isNull();
                    assertThat(seat.getHoldExpiresAt()).isNull();
                });
    }

    @Test
    @DisplayName("만료된 HELD 좌석은 재선점이 가능해야 한다")
    void reserveExpiredHeldSeat_succeeds() {
        Long scheduleId = createSchedule(CONCERT_ID, LocalDate.of(2026, 4, 13));
        LocalDateTime expiredAt = LocalDateTime.now().minusMinutes(1);

        seatInventoryJpaRepository.save(
                new SeatInventoryJpaEntity(scheduleId, 13, SeatStatus.HELD, 888L, expiredAt, null)
        );
        queueTokenJpaRepository.save(createQueueToken("qt-expired", 101L, CONCERT_ID, 1L, QueueTokenStatus.ACTIVE));

        LocalDateTime before = LocalDateTime.now();
        reservationFacade.reserve(new ReserveSeatCommand("qt-expired", 101L, scheduleId, 13));
        LocalDateTime after = LocalDateTime.now();

        assertThat(reservationJpaRepository.count()).isEqualTo(1);
        assertThat(seatInventoryJpaRepository.findByScheduleIdAndSeatNumber(scheduleId, 13))
                .isPresent()
                .get()
                .satisfies(seat -> {
                    assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
                    assertThat(seat.getHeldByUserId()).isEqualTo(101L);
                    assertThat(seat.getHoldExpiresAt()).isAfter(before.plusMinutes(4));
                    assertThat(seat.getHoldExpiresAt()).isBefore(after.plusMinutes(6));
                });
    }

    @Test
    @DisplayName("동일 사용자가 이미 활성 임시 예약을 가진 경우 실패해야 한다")
    void reserveWhenUserAlreadyHasTemporaryReservation_fails() {
        Long scheduleId = createSchedule(CONCERT_ID, LocalDate.of(2026, 4, 14));

        reservationJpaRepository.save(new ReservationJpaEntity(
                101L,
                scheduleId,
                1,
                ReservationStatus.TEMPORARY,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(4),
                null
        ));
        seatInventoryJpaRepository.save(
                new SeatInventoryJpaEntity(scheduleId, 14, SeatStatus.AVAILABLE, null, null, null)
        );
        queueTokenJpaRepository.save(createQueueToken("qt-user-held", 101L, CONCERT_ID, 1L, QueueTokenStatus.ACTIVE));

        assertThatThrownBy(() -> reservationFacade.reserve(new ReserveSeatCommand("qt-user-held", 101L, scheduleId, 14)))
                .isInstanceOf(UserAlreadyHasHeldSeatException.class);

        assertThat(reservationJpaRepository.count()).isEqualTo(1);
        assertThat(seatInventoryJpaRepository.findByScheduleIdAndSeatNumber(scheduleId, 14))
                .isPresent()
                .get()
                .satisfies(seat -> {
                    assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
                    assertThat(seat.getHeldByUserId()).isNull();
                    assertThat(seat.getHoldExpiresAt()).isNull();
                });
    }

    @Test
    @DisplayName("ACTIVE가 아닌 토큰으로 예약 시 실패해야 한다")
    void reserveWithNonActiveToken_fails() {
        Long scheduleId = createSchedule(CONCERT_ID, LocalDate.of(2026, 4, 15));

        seatInventoryJpaRepository.save(
                new SeatInventoryJpaEntity(scheduleId, 15, SeatStatus.AVAILABLE, null, null, null)
        );
        queueTokenJpaRepository.save(createQueueToken("qt-waiting", 101L, CONCERT_ID, 1L, QueueTokenStatus.WAITING));

        assertThatThrownBy(() -> reservationFacade.reserve(new ReserveSeatCommand("qt-waiting", 101L, scheduleId, 15)))
                .isInstanceOf(QueueTokenNotActiveException.class);

        assertThat(reservationJpaRepository.count()).isZero();
        assertThat(seatInventoryJpaRepository.findByScheduleIdAndSeatNumber(scheduleId, 15))
                .isPresent()
                .get()
                .satisfies(seat -> {
                    assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
                    assertThat(seat.getHeldByUserId()).isNull();
                    assertThat(seat.getHoldExpiresAt()).isNull();
                });
    }

    private Callable<Boolean> tryReserve(CountDownLatch startLatch, ReserveSeatCommand command) {
        return () -> {
            startLatch.await();
            try {
                reservationFacade.reserve(command);
                return true;
            } catch (Exception e) {
                return false;
            }
        };
    }

    private Long createSchedule(Long concertId, LocalDate concertDate) {
        return concertScheduleJpaRepository.save(new ConcertScheduleJpaEntity(concertId, concertDate)).getId();
    }

    private QueueTokenJpaEntity createQueueToken(
            String token,
            Long userId,
            Long concertId,
            Long queuePosition,
            QueueTokenStatus status
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new QueueTokenJpaEntity(
                token,
                userId,
                concertId,
                queuePosition,
                status,
                now.minusMinutes(1),
                status == QueueTokenStatus.ACTIVE ? now.minusSeconds(30) : null,
                null
        );
    }
}
