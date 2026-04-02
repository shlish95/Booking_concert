package kr.hhplus.be.server.infrastructure.persistence.repository;

import kr.hhplus.be.server.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, Long> {

    Optional<PaymentJpaEntity> findByReservationId(Long reservationId);
}
