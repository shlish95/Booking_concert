package kr.hhplus.be.server.infrastructure.persistence.adapter;

import kr.hhplus.be.server.application.concert.port.out.ConcertQueryPort;
import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import kr.hhplus.be.server.infrastructure.persistence.mapper.ConcertScheduleMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.persistence.repository.SeatInventoryJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("!mock")
public class ConcertQueryPersistenceAdapter implements ConcertQueryPort {

    private final ConcertScheduleJpaRepository concertScheduleJpaRepository;
    private final SeatInventoryJpaRepository seatInventoryJpaRepository;
    private final ConcertScheduleMapper concertScheduleMapper;

    public ConcertQueryPersistenceAdapter(
            ConcertScheduleJpaRepository concertScheduleJpaRepository,
            SeatInventoryJpaRepository seatInventoryJpaRepository,
            ConcertScheduleMapper concertScheduleMapper
    ) {
        this.concertScheduleJpaRepository = concertScheduleJpaRepository;
        this.seatInventoryJpaRepository = seatInventoryJpaRepository;
        this.concertScheduleMapper = concertScheduleMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConcertSchedule> getSchedules(Long concertId) {
        return concertScheduleJpaRepository.findAllByConcertIdOrderByConcertDateAsc(concertId).stream()
                .map(concertScheduleMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getAvailableSeatNumbers(Long scheduleId) {
        return seatInventoryJpaRepository.findAvailableSeatNumbers(scheduleId, LocalDateTime.now());
    }
}
