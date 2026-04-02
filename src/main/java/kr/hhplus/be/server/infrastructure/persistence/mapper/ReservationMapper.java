package kr.hhplus.be.server.infrastructure.persistence.mapper;

import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.infrastructure.persistence.entity.ReservationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public Reservation toDomain(ReservationJpaEntity entity) {
        return new Reservation(
                entity.getId(),
                entity.getUserId(),
                entity.getScheduleId(),
                entity.getSeatNumber(),
                entity.getStatus(),
                entity.getExpiresAt()
        );
    }
}
