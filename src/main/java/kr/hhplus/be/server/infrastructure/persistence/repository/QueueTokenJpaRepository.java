package kr.hhplus.be.server.infrastructure.persistence.repository;

import kr.hhplus.be.server.infrastructure.persistence.entity.QueueTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QueueTokenJpaRepository extends JpaRepository<QueueTokenJpaEntity, Long> {

    Optional<QueueTokenJpaEntity> findByConcertIdAndToken(Long concertId, String token);

    @Query("""
            select coalesce(max(q.queuePosition), 0) + 1
            from QueueTokenJpaEntity q
            where q.concertId = :concertId
            """)
    Long findNextQueuePosition(@Param("concertId") Long concertId);
}
