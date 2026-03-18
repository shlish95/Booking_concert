package kr.hhplus.be.server.presentation.balance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "잔액 응답")
public record BalanceResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "현재 잔액", example = "150000")
        Long balance
) {
}
