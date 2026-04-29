package kr.hhplus.be.server.application.queue.port.out;

import kr.hhplus.be.server.domain.queue.QueueToken;

import java.util.List;

public interface QueueTokenPort {

    QueueToken issue(Long concertId, Long userId);

    Long getWaitingPosition(Long concertId, String token);

    List<QueueToken> activateTopWaiting(Long concertId, int maxActiveCount);

    QueueToken findByConcertIdAndToken(Long concertId, String token);
}
