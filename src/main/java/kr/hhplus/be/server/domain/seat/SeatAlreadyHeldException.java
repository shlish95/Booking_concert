package kr.hhplus.be.server.domain.seat;

import kr.hhplus.be.server.domain.common.DomainException;

public class SeatAlreadyHeldException extends DomainException {

    public SeatAlreadyHeldException() {
        super("이미 선점된 좌석입니다.");
    }
}
