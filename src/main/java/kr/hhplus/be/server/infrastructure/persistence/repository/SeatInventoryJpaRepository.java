package kr.hhplus.be.server.infrastructure.persistence.repository;

import kr.hhplus.be.server.infrastructure.persistence.entity.SeatInventoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatInventoryJpaRepository extends JpaRepository<SeatInventoryJpaEntity, Long> {

    Optional<SeatInventoryJpaEntity> findByScheduleIdAndSeatNumber(Long scheduleId, Integer seatNumber);

    @Query("""
            select s.seatNumber
            from SeatInventoryJpaEntity s
            where s.scheduleId = :scheduleId
              and (
                    s.status = kr.hhplus.be.server.domain.seat.SeatStatus.AVAILABLE
                    or (
                        s.status = kr.hhplus.be.server.domain.seat.SeatStatus.HELD
                        and s.holdExpiresAt < :now
                    )
              )
            order by s.seatNumber asc
            """)
    List<Integer> findAvailableSeatNumbers(
            @Param("scheduleId") Long scheduleId,
            @Param("now") LocalDateTime now
    );
}
