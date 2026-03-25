package kr.hhplus.be.server.infrastructure.persistence.repository;

import kr.hhplus.be.server.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByUuid(String uuid);
}
