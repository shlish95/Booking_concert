package kr.hhplus.be.server.domain.balance;

import kr.hhplus.be.server.domain.common.DomainException;

public class OptimisticLockConflictException extends DomainException {

    public OptimisticLockConflictException() {
        super("낙관적 락 충돌이 발생했습니다.");
    }
}
