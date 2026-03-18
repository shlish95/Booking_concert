package kr.hhplus.be.server.presentation.concert.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "예약 가능 좌석 응답")
public record AvailableSeatsResponse(
        @Schema(description = "회차 ID", example = "100")
        Long scheduleId,
        @ArraySchema(schema = @Schema(description = "예약 가능 좌석 번호", example = "1"))
        List<Integer> availableSeats
) {
}
