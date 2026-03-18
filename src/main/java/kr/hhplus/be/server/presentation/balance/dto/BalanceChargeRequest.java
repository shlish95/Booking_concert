package kr.hhplus.be.server.presentation.balance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "잔액 충전 요청")
public record BalanceChargeRequest(
        @Schema(description = "충전 금액", example = "100000")
        Long amount
) {
}
