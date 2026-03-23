package kr.hhplus.be.server.domain.reservation;

import kr.hhplus.be.server.domain.common.DomainException;

public class UserAlreadyHasHeldSeatException extends DomainException {

    public UserAlreadyHasHeldSeatException() {
        super("사용자는 동시에 하나의 임시 배정만 가질 수 있습니다.");
    }
}
