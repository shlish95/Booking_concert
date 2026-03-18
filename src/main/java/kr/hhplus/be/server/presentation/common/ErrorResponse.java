package kr.hhplus.be.server.presentation.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 에러 응답")
public record ErrorResponse(
        @Schema(description = "에러 코드", example = "SEAT_ALREADY_HELD")
        String code,
        @Schema(description = "에러 메시지", example = "이미 선점된 좌석입니다.")
        String message
) {
}
