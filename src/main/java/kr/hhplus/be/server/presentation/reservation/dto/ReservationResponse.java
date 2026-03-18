package kr.hhplus.be.server.presentation.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "좌석 예약 응답")
public record ReservationResponse(
        @Schema(description = "예약 ID", example = "5001")
        Long reservationId,
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "회차 ID", example = "100")
        Long scheduleId,
        @Schema(description = "좌석 번호", example = "12")
        Integer seatNumber,
        @Schema(description = "예약 상태", example = "TEMPORARY")
        String status,
        @Schema(description = "만료 시각", example = "2026-03-12T10:05:00")
        LocalDateTime expiresAt
) {
}
