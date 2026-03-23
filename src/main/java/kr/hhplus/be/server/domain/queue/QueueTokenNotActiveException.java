package kr.hhplus.be.server.domain.queue;

import kr.hhplus.be.server.domain.common.DomainException;

public class QueueTokenNotActiveException extends DomainException {

    public QueueTokenNotActiveException() {
        super("ACTIVE 상태의 대기열 토큰만 사용할 수 있습니다.");
    }
}
