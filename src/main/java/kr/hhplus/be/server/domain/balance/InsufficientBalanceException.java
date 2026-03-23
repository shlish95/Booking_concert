package kr.hhplus.be.server.domain.balance;

import kr.hhplus.be.server.domain.common.DomainException;

public class InsufficientBalanceException extends DomainException {

    public InsufficientBalanceException() {
        super("잔액이 부족합니다.");
    }
}
