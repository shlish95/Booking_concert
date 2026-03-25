package kr.hhplus.be.server.infrastructure.mock.concert;

import kr.hhplus.be.server.application.concert.port.out.ConcertQueryPort;
import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("mock")
public class MockConcertQueryAdapter implements ConcertQueryPort {

    @Override
    public List<ConcertSchedule> getSchedules(Long concertId) {
        return List.of(
                new ConcertSchedule(100L, concertId, LocalDate.of(2026, 4, 1)),
                new ConcertSchedule(101L, concertId, LocalDate.of(2026, 4, 2))
        );
    }

    @Override
    public List<Integer> getAvailableSeatNumbers(Long scheduleId) {
        return List.of(1, 2, 3, 7, 8, 9, 12, 13);
    }
}
