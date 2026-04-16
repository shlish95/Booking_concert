package kr.hhplus.be.server.domain.reservation;

import kr.hhplus.be.server.domain.common.DomainException;

public class ConcurrentReservationRequestException extends DomainException {

    public ConcurrentReservationRequestException() {
        super("동일 사용자의 예약 요청이 이미 처리 중입니다.");
    }
}
