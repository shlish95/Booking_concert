package kr.hhplus.be.server.application.payment.facade;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.application.payment.dto.PayReservationCommand;
import kr.hhplus.be.server.application.ranking.facade.SoldOutRankingFacade;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.infrastructure.persistence.adapter.BalancePersistenceAdapter;
import kr.hhplus.be.server.infrastructure.persistence.adapter.PaymentPersistenceAdapter;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertScheduleJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.ReservationJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.SeatInventoryJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.UserJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.PaymentMapper;
import kr.hhplus.be.server.infrastructure.persistence.mapper.UserBalanceMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.PaymentJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.ReservationJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.SeatInventoryJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.UserJpaRepository;
import kr.hhplus.be.server.infrastructure.redis.ranking.RedisSoldOutRankingAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({
        TestcontainersConfiguration.class,
        PaymentFacade.class,
        BalancePersistenceAdapter.class,
        PaymentPersistenceAdapter.class,
        RedisSoldOutRankingAdapter.class,
        SoldOutRankingFacade.class,
        UserBalanceMapper.class,
        PaymentMapper.class
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentFacadeRankingIntegrationTest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private SoldOutRankingFacade soldOutRankingFacade;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ConcertJpaRepository concertJpaRepository;

    @Autowired
    private ConcertScheduleJpaRepository concertScheduleJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private SeatInventoryJpaRepository seatInventoryJpaRepository;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        paymentJpaRepository.deleteAll();
        reservationJpaRepository.deleteAll();
        seatInventoryJpaRepository.deleteAll();
        concertScheduleJpaRepository.deleteAll();
        concertJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    @DisplayName("결제 성공 후 잔여 좌석이 0이면 빠른 매진 랭킹에 적재된다")
    void pay_whenSoldOut_recordsRanking() {
        UserJpaEntity user = userJpaRepository.save(new UserJpaEntity("user-1", 20_000L));
        ConcertJpaEntity concert = concertJpaRepository.save(new ConcertJpaEntity("concert-1", 7_000L));
        ConcertScheduleJpaEntity schedule = concertScheduleJpaRepository.save(
                new ConcertScheduleJpaEntity(concert.getId(), LocalDate.of(2026, 4, 30))
        );
        ReservationJpaEntity reservation = reservationJpaRepository.save(
                new ReservationJpaEntity(
                        user.getId(),
                        schedule.getId(),
                        1,
                        ReservationStatus.TEMPORARY,
                        LocalDateTime.now().minusMinutes(2),
                        LocalDateTime.now().plusMinutes(3),
                        null
                )
        );
        seatInventoryJpaRepository.save(
                new SeatInventoryJpaEntity(
                        schedule.getId(),
                        1,
                        SeatStatus.HELD,
                        user.getId(),
                        LocalDateTime.now().plusMinutes(3),
                        null
                )
        );

        paymentFacade.pay(new PayReservationCommand("ignored", user.getId(), reservation.getId()));

        assertThat(paymentJpaRepository.count()).isEqualTo(1);
        assertThat(paymentJpaRepository.findAll().get(0).getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(soldOutRankingFacade.getTop(10))
                .extracting("scheduleId")
                .containsExactly(schedule.getId());
    }

    @Test
    @DisplayName("완판되지 않은 회차는 빠른 매진 랭킹에 포함되지 않는다")
    void pay_whenNotSoldOut_doesNotRecordRanking() {
        UserJpaEntity user = userJpaRepository.save(new UserJpaEntity("user-2", 20_000L));
        ConcertJpaEntity concert = concertJpaRepository.save(new ConcertJpaEntity("concert-2", 7_000L));
        ConcertScheduleJpaEntity schedule = concertScheduleJpaRepository.save(
                new ConcertScheduleJpaEntity(concert.getId(), LocalDate.of(2026, 5, 1))
        );
        ReservationJpaEntity reservation = reservationJpaRepository.save(
                new ReservationJpaEntity(
                        user.getId(),
                        schedule.getId(),
                        1,
                        ReservationStatus.TEMPORARY,
                        LocalDateTime.now().minusMinutes(2),
                        LocalDateTime.now().plusMinutes(3),
                        null
                )
        );
        seatInventoryJpaRepository.save(
                new SeatInventoryJpaEntity(
                        schedule.getId(),
                        1,
                        SeatStatus.HELD,
                        user.getId(),
                        LocalDateTime.now().plusMinutes(3),
                        null
                )
        );
        seatInventoryJpaRepository.save(
                new SeatInventoryJpaEntity(
                        schedule.getId(),
                        2,
                        SeatStatus.AVAILABLE,
                        null,
                        null,
                        null
                )
        );

        paymentFacade.pay(new PayReservationCommand("ignored", user.getId(), reservation.getId()));

        assertThat(paymentJpaRepository.count()).isEqualTo(1);
        assertThat(soldOutRankingFacade.getTop(10)).isEmpty();
    }
}
