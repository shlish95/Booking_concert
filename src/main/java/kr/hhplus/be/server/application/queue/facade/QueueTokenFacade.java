package kr.hhplus.be.server.application.queue.facade;

import kr.hhplus.be.server.application.queue.dto.GetQueueTokenQuery;
import kr.hhplus.be.server.application.queue.dto.IssueQueueTokenCommand;
import kr.hhplus.be.server.application.queue.dto.QueueTokenResult;
import kr.hhplus.be.server.application.queue.port.out.QueueTokenPort;
import kr.hhplus.be.server.application.queue.usecase.GetQueueTokenUseCase;
import kr.hhplus.be.server.application.queue.usecase.IssueQueueTokenUseCase;
import kr.hhplus.be.server.domain.queue.QueueToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueTokenFacade implements IssueQueueTokenUseCase, GetQueueTokenUseCase {

    private final QueueTokenPort queueTokenPort;

    public QueueTokenFacade(QueueTokenPort queueTokenPort) {
        this.queueTokenPort = queueTokenPort;
    }

    @Override
    public QueueTokenResult issue(IssueQueueTokenCommand command) {
        return toResult(queueTokenPort.issue(command.concertId(), command.userId()));
    }

    @Override
    public QueueTokenResult get(GetQueueTokenQuery query) {
        return toResult(queueTokenPort.findByConcertIdAndToken(query.concertId(), query.token()));
    }

    public List<QueueTokenResult> activateTopWaiting(Long concertId, int maxActiveCount) {
        return queueTokenPort.activateTopWaiting(concertId, maxActiveCount).stream()
                .map(this::toResult)
                .toList();
    }

    private QueueTokenResult toResult(QueueToken queueToken) {
        return new QueueTokenResult(
                queueToken.token(),
                queueToken.concertId(),
                queueToken.userId(),
                queueToken.queuePosition(),
                queueToken.status().name(),
                queueToken.issuedAt(),
                queueToken.activatedAt(),
                queueToken.expiredAt()
        );
    }
}
