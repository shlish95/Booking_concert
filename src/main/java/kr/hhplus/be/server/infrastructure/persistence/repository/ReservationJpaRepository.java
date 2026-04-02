package kr.hhplus.be.server.infrastructure.persistence.repository;

import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.infrastructure.persistence.entity.ReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, Long> {

    Optional<ReservationJpaEntity> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndStatusAndExpiresAtAfter(
            Long userId,
            ReservationStatus status,
            LocalDateTime expiresAt
    );
}
