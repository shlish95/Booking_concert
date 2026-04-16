package kr.hhplus.be.server.infrastructure.persistence.adapter;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertScheduleJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.SeatInventoryJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.ConcertScheduleMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.SeatInventoryJpaRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({
        TestcontainersConfiguration.class,
        ConcertQueryPersistenceAdapter.class,
        ConcertScheduleMapper.class
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ConcertQueryPersistenceAdapterTest {

    @Autowired
    private ConcertQueryPersistenceAdapter concertQueryPersistenceAdapter;

    @Autowired
    private ConcertScheduleJpaRepository concertScheduleJpaRepository;

    @Autowired
    private SeatInventoryJpaRepository seatInventoryJpaRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        seatInventoryJpaRepository.deleteAll();
        concertScheduleJpaRepository.deleteAll();
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    @DisplayName("콘서트의 회차 목록을 날짜 오름차순으로 조회한다")
    void getSchedules() {
        Long concertId = 10L;

        concertScheduleJpaRepository.saveAll(List.of(
                new ConcertScheduleJpaEntity(concertId, LocalDate.of(2026, 4, 3)),
                new ConcertScheduleJpaEntity(concertId, LocalDate.of(2026, 4, 1)),
                new ConcertScheduleJpaEntity(concertId, LocalDate.of(2026, 4, 2)),
                new ConcertScheduleJpaEntity(20L, LocalDate.of(2026, 4, 1))
        ));

        List<ConcertSchedule> schedules = concertQueryPersistenceAdapter.getSchedules(concertId);

        assertThat(schedules)
                .extracting(ConcertSchedule::concertDate)
                .containsExactly(
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 4, 2),
                        LocalDate.of(2026, 4, 3)
                );
    }

    @Test
    @DisplayName("콘서트의 회차 목록은 첫 조회 후 Redis 캐시에 저장되고 다음 조회는 캐시에서 반환한다")
    void getSchedulesUsesCacheAside() {
        Long concertId = 30L;

        concertScheduleJpaRepository.saveAll(List.of(
                new ConcertScheduleJpaEntity(concertId, LocalDate.of(2026, 5, 3)),
                new ConcertScheduleJpaEntity(concertId, LocalDate.of(2026, 5, 1)),
                new ConcertScheduleJpaEntity(concertId, LocalDate.of(2026, 5, 2))
        ));

        List<ConcertSchedule> firstResult = concertQueryPersistenceAdapter.getSchedules(concertId);
        String cacheKey = "concert:schedules:" + concertId;

        assertThat(firstResult).hasSize(3);
        assertThat(stringRedisTemplate.hasKey(cacheKey)).isTrue();
        assertThat(stringRedisTemplate.getExpire(cacheKey)).isPositive();

        concertScheduleJpaRepository.deleteAll();

        List<ConcertSchedule> secondResult = concertQueryPersistenceAdapter.getSchedules(concertId);

        assertThat(secondResult)
                .extracting(ConcertSchedule::concertDate)
                .containsExactly(
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 2),
                        LocalDate.of(2026, 5, 3)
                );
    }

    @Test
    @DisplayName("회차의 예약 가능한 좌석 번호를 조회한다")
    void getAvailableSeatNumbers() {
        Long scheduleId = 100L;
        LocalDateTime now = LocalDateTime.now();

        seatInventoryJpaRepository.saveAll(List.of(
                new SeatInventoryJpaEntity(scheduleId, 1, SeatStatus.AVAILABLE, null, null, null),
                new SeatInventoryJpaEntity(scheduleId, 2, SeatStatus.HELD, 10L, now.minusMinutes(1), null),
                new SeatInventoryJpaEntity(scheduleId, 3, SeatStatus.HELD, 11L, now.plusMinutes(10), null),
                new SeatInventoryJpaEntity(scheduleId, 4, SeatStatus.RESERVED, null, null, 12L),
                new SeatInventoryJpaEntity(200L, 1, SeatStatus.AVAILABLE, null, null, null)
        ));

        List<Integer> availableSeats = concertQueryPersistenceAdapter.getAvailableSeatNumbers(scheduleId);

        assertThat(availableSeats).containsExactly(1, 2);
    }
}
