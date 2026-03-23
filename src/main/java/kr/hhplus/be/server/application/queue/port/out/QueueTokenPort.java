package kr.hhplus.be.server.application.queue.port.out;

import kr.hhplus.be.server.domain.queue.QueueToken;

public interface QueueTokenPort {

    QueueToken issue(Long concertId, Long userId);

    QueueToken findByConcertIdAndToken(Long concertId, String token);
}
