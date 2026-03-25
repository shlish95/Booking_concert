package kr.hhplus.be.server.infrastructure.persistence.mapper;

import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertScheduleJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ConcertScheduleMapper {

    public ConcertSchedule toDomain(ConcertScheduleJpaEntity entity) {
        return new ConcertSchedule(
                entity.getId(),
                entity.getConcertId(),
                entity.getConcertDate()
        );
    }
}
