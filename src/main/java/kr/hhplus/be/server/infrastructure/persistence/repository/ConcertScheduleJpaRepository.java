package kr.hhplus.be.server.infrastructure.persistence.repository;

import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertScheduleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertScheduleJpaRepository extends JpaRepository<ConcertScheduleJpaEntity, Long> {

    List<ConcertScheduleJpaEntity> findAllByConcertIdOrderByConcertDateAsc(Long concertId);
}
