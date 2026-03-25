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
import kr.hhplus.be.server.domain.queue.QueueTokenStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "queue_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_queue_token_token", columnNames = "token")
        },
        indexes = {
                @Index(name = "idx_queue_token_concert_status_position", columnList = "concert_id, status, queue_position")
        }
)
public class QueueTokenJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, length = 100)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "concert_id", nullable = false)
    private Long concertId;

    @Column(name = "queue_position", nullable = false)
    private Long queuePosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QueueTokenStatus status;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    protected QueueTokenJpaEntity() {
    }

    public QueueTokenJpaEntity(
            String token,
            Long userId,
            Long concertId,
            Long queuePosition,
            QueueTokenStatus status,
            LocalDateTime issuedAt,
            LocalDateTime activatedAt,
            LocalDateTime expiredAt
    ) {
        this.token = token;
        this.userId = userId;
        this.concertId = concertId;
        this.queuePosition = queuePosition;
        this.status = status;
        this.issuedAt = issuedAt;
        this.activatedAt = activatedAt;
        this.expiredAt = expiredAt;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getConcertId() {
        return concertId;
    }

    public Long getQueuePosition() {
        return queuePosition;
    }

    public QueueTokenStatus getStatus() {
        return status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void markUsed(LocalDateTime at) {
        this.status = QueueTokenStatus.USED;
        this.expiredAt = at;
    }

    public void activate(LocalDateTime at) {
        this.status = QueueTokenStatus.ACTIVE;
        this.activatedAt = at;
    }
}
