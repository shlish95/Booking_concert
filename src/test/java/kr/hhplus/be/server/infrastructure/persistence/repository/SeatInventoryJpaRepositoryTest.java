package kr.hhplus.be.server.infrastructure.persistence.repository;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.infrastructure.persistence.entity.SeatInventoryJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SeatInventoryJpaRepositoryTest {

    @Autowired
    private SeatInventoryJpaRepository seatInventoryJpaRepository;

    @Test
    @DisplayName("특정 회차에서 예약 가능한 좌석만 조회한다")
    void findAvailableSeatNumbers() {
        Long targetScheduleId = 100L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 24, 10, 0, 0);

        seatInventoryJpaRepository.saveAll(List.of(
                new SeatInventoryJpaEntity(targetScheduleId, 1, SeatStatus.AVAILABLE, null, null, null),
                new SeatInventoryJpaEntity(targetScheduleId, 2, SeatStatus.HELD, 10L, now.minusMinutes(1), null),
                new SeatInventoryJpaEntity(targetScheduleId, 3, SeatStatus.HELD, 11L, now.plusMinutes(3), null),
                new SeatInventoryJpaEntity(targetScheduleId, 4, SeatStatus.RESERVED, null, null, 12L),
                new SeatInventoryJpaEntity(200L, 1, SeatStatus.AVAILABLE, null, null, null)
        ));

        List<Integer> availableSeatNumbers = seatInventoryJpaRepository.findAvailableSeatNumbers(targetScheduleId, now);

        assertThat(availableSeatNumbers).containsExactly(1, 2);
    }
}
