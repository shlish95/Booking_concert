package kr.hhplus.be.server.domain.seat;

import kr.hhplus.be.server.domain.common.DomainException;

public class SeatAlreadyReservedException extends DomainException {

    public SeatAlreadyReservedException() {
        super("이미 예약 확정된 좌석입니다.");
    }
}
