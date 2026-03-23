package kr.hhplus.be.server.domain.reservation;

import kr.hhplus.be.server.domain.common.DomainException;

public class ReservationExpiredException extends DomainException {

    public ReservationExpiredException() {
        super("만료된 예약입니다.");
    }
}
