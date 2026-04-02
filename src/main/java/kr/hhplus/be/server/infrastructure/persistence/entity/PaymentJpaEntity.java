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
import kr.hhplus.be.server.domain.payment.PaymentStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_reservation", columnList = "reservation_id")
        }
)
public class PaymentJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    protected PaymentJpaEntity() {
    }

    public PaymentJpaEntity(Long userId, Long reservationId, Long amount, PaymentStatus status, LocalDateTime paidAt) {
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.status = status;
        this.paidAt = paidAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
