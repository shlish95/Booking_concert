package kr.hhplus.be.server.presentation.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 요청")
public record PaymentCreateRequest(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "예약 ID", example = "5001")
        Long reservationId
) {
}
