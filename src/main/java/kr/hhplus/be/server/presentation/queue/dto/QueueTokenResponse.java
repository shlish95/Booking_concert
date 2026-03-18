package kr.hhplus.be.server.presentation.queue.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "대기열 토큰 응답")
public record QueueTokenResponse(
        @Schema(description = "토큰 값", example = "qt_mock_10_1")
        String token,
        @Schema(description = "콘서트 ID", example = "10")
        Long concertId,
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "대기 순번", example = "125")
        Long queuePosition,
        @Schema(description = "토큰 상태", example = "WAITING")
        String status,
        @Schema(description = "발급 시각", example = "2026-03-12T10:00:00")
        LocalDateTime issuedAt,
        @Schema(description = "활성 시각", nullable = true)
        LocalDateTime activatedAt,
        @Schema(description = "만료 시각", nullable = true)
        LocalDateTime expiredAt
) {
}
