package kr.hhplus.be.server.application.reservation.facade;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.application.reservation.dto.ReserveSeatCommand;
import kr.hhplus.be.server.domain.queue.QueueTokenStatus;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.infrastructure.persistence.adapter.ConcertQueryPersistenceAdapter;
import kr.hhplus.be.server.infrastructure.persistence.adapter.QueueTokenPersistenceAdapter;
import kr.hhplus.be.server.infrastructure.persistence.adapter.ReservationPersistenceAdapter;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertScheduleJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.QueueTokenJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.SeatInventoryJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.ConcertScheduleMapper;
import kr.hhplus.be.server.infrastructure.persistence.mapper.QueueTokenMapper;
import kr.hhplus.be.server.infrastructure.persistence.mapper.ReservationMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.QueueTokenJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.ReservationJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.SeatInventoryJpaRepository;
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

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("동일 좌석에 대한 동시 예약 요청은 한 건만 성공한다")
    void reserveSameSeatConcurrently_onlyOneSucceeds() throws Exception {
        Long concertId = 1L;
        Long scheduleId = concertScheduleJpaRepository.save(
                new ConcertScheduleJpaEntity(concertId, LocalDate.of(2026, 4, 10))
        ).getId();

        seatInventoryJpaRepository.save(
                new SeatInventoryJpaEntity(scheduleId, 7, SeatStatus.AVAILABLE, null, null, null)
        );

        queueTokenJpaRepository.saveAll(List.of(
                new QueueTokenJpaEntity(
                        "qt-1",
                        101L,
                        concertId,
                        1L,
                        QueueTokenStatus.ACTIVE,
                        LocalDateTime.now().minusMinutes(1),
                        LocalDateTime.now().minusSeconds(30),
                        null
                ),
                new QueueTokenJpaEntity(
                        "qt-2",
                        102L,
                        concertId,
                        2L,
                        QueueTokenStatus.ACTIVE,
                        LocalDateTime.now().minusMinutes(1),
                        LocalDateTime.now().minusSeconds(30),
                        null
                )
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
}
