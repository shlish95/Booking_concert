package kr.hhplus.be.server.infrastructure.persistence.repository;

import kr.hhplus.be.server.infrastructure.persistence.entity.ConcertJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertJpaRepository extends JpaRepository<ConcertJpaEntity, Long> {
}
