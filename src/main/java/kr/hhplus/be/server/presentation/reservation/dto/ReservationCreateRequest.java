package kr.hhplus.be.server.presentation.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 예약 요청")
public record ReservationCreateRequest(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "회차 ID", example = "100")
        Long scheduleId,
        @Schema(description = "좌석 번호", example = "12")
        Integer seatNumber
) {
}
