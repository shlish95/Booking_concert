package kr.hhplus.be.server.domain.balance;

public record UserBalance(
        Long userId,
        Long amount,
        Long version
) {
}
