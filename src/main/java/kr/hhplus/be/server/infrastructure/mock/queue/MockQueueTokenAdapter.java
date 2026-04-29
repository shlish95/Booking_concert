package kr.hhplus.be.server.infrastructure.mock.queue;

import kr.hhplus.be.server.application.queue.port.out.QueueTokenPort;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@Profile("mock")
public class MockQueueTokenAdapter implements QueueTokenPort {

    @Override
    public QueueToken issue(Long concertId, Long userId) {
        return new QueueToken(
                "qt_mock_" + concertId + "_" + userId,
                concertId,
                userId,
                125L,
                QueueTokenStatus.WAITING,
                LocalDateTime.of(2026, 3, 12, 10, 0, 0),
                null,
                null
        );
    }

    @Override
    public QueueToken findByConcertIdAndToken(Long concertId, String token) {
        return new QueueToken(
                token,
                concertId,
                1L,
                125L,
                QueueTokenStatus.WAITING,
                LocalDateTime.of(2026, 3, 12, 10, 0, 0),
                null,
                null
        );
    }

    @Override
    public Long getWaitingPosition(Long concertId, String token) {
        return 125L;
    }

    @Override
    public List<QueueToken> activateTopWaiting(Long concertId, int maxActiveCount) {
        return Collections.emptyList();
    }
}
