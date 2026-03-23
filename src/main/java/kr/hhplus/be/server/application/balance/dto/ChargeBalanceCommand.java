package kr.hhplus.be.server.application.balance.dto;

public record ChargeBalanceCommand(
        Long userId,
        Long amount
) {
}
