package kr.hhplus.be.server.presentation.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "결제 응답")
public record PaymentResponse(
        @Schema(description = "결제 ID", example = "9001")
        Long paymentId,
        @Schema(description = "예약 ID", example = "5001")
        Long reservationId,
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "결제 금액", example = "50000")
        Long amount,
        @Schema(description = "결제 상태", example = "SUCCESS")
        String status,
        @Schema(description = "결제 시각", example = "2026-03-12T10:02:00")
        LocalDateTime paidAt
) {
}
