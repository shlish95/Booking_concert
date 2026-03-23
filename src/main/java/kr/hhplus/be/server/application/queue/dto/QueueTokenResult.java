package kr.hhplus.be.server.application.queue.dto;

import java.time.LocalDateTime;

public record QueueTokenResult(
        String token,
        Long concertId,
        Long userId,
        Long queuePosition,
        String status,
        LocalDateTime issuedAt,
        LocalDateTime activatedAt,
        LocalDateTime expiredAt
) {
}
