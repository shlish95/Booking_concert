package kr.hhplus.be.server.presentation.concert.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "예약 가능 회차 응답")
public record ScheduleSummaryResponse(
        @Schema(description = "회차 ID", example = "100")
        Long scheduleId,
        @Schema(description = "콘서트 ID", example = "10")
        Long concertId,
        @Schema(description = "공연 날짜", example = "2026-04-01")
        LocalDate concertDate
) {
}
