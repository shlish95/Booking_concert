package kr.hhplus.be.server.domain.queue;

import java.time.LocalDateTime;

public record QueueToken(
        String token,
        Long concertId,
        Long userId,
        Long queuePosition,
        QueueTokenStatus status,
        LocalDateTime issuedAt,
        LocalDateTime activatedAt,
        LocalDateTime expiredAt
) {
}
