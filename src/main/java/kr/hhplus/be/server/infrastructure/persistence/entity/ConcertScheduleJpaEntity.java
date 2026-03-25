package kr.hhplus.be.server.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

@Entity
@Table(
        name = "concert_schedules",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_concert_schedule_concert_date", columnNames = {"concert_id", "concert_date"})
        },
        indexes = {
                @Index(name = "idx_concert_schedule_concert_date", columnList = "concert_id, concert_date")
        }
)
public class ConcertScheduleJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_id", nullable = false)
    private Long concertId;

    @Column(name = "concert_date", nullable = false)
    private LocalDate concertDate;

    protected ConcertScheduleJpaEntity() {
    }

    public ConcertScheduleJpaEntity(Long concertId, LocalDate concertDate) {
        this.concertId = concertId;
        this.concertDate = concertDate;
    }

    public Long getId() {
        return id;
    }

    public Long getConcertId() {
        return concertId;
    }

    public LocalDate getConcertDate() {
        return concertDate;
    }
}
