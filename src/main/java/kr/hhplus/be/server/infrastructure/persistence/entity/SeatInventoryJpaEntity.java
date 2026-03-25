package kr.hhplus.be.server.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import kr.hhplus.be.server.domain.seat.SeatStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "seat_inventories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seat_inventory_schedule_seat", columnNames = {"schedule_id", "seat_number"})
        },
        indexes = {
                @Index(name = "idx_seat_inventory_schedule_status_exp", columnList = "schedule_id, status, hold_expires_at")
        }
)
public class SeatInventoryJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeatStatus status;

    @Column(name = "held_by_user_id")
    private Long heldByUserId;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Column(name = "reserved_by_user_id")
    private Long reservedByUserId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected SeatInventoryJpaEntity() {
    }

    public SeatInventoryJpaEntity(
            Long scheduleId,
            Integer seatNumber,
            SeatStatus status,
            Long heldByUserId,
            LocalDateTime holdExpiresAt,
            Long reservedByUserId
    ) {
        this.scheduleId = scheduleId;
        this.seatNumber = seatNumber;
        this.status = status;
        this.heldByUserId = heldByUserId;
        this.holdExpiresAt = holdExpiresAt;
        this.reservedByUserId = reservedByUserId;
    }

    public Long getId() {
        return id;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public Long getHeldByUserId() {
        return heldByUserId;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public Long getReservedByUserId() {
        return reservedByUserId;
    }

    public Long getVersion() {
        return version;
    }

    public void hold(Long heldByUserId, LocalDateTime holdExpiresAt) {
        this.status = SeatStatus.HELD;
        this.heldByUserId = heldByUserId;
        this.holdExpiresAt = holdExpiresAt;
        this.reservedByUserId = null;
    }

    public void reserve(Long reservedByUserId) {
        this.status = SeatStatus.RESERVED;
        this.reservedByUserId = reservedByUserId;
        this.heldByUserId = null;
        this.holdExpiresAt = null;
    }

}
