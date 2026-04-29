package kr.hhplus.be.server.infrastructure.persistence.adapter;

import kr.hhplus.be.server.application.queue.port.out.QueueTokenPort;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenStatus;
import kr.hhplus.be.server.infrastructure.persistence.entity.QueueTokenJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.QueueTokenMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.QueueTokenJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@Profile("!mock")
public class QueueTokenPersistenceAdapter implements QueueTokenPort {

    private final QueueTokenJpaRepository queueTokenJpaRepository;
    private final QueueTokenMapper queueTokenMapper;

    public QueueTokenPersistenceAdapter(
            QueueTokenJpaRepository queueTokenJpaRepository,
            QueueTokenMapper queueTokenMapper
    ) {
        this.queueTokenJpaRepository = queueTokenJpaRepository;
        this.queueTokenMapper = queueTokenMapper;
    }

    @Override
    @Transactional
    public QueueToken issue(Long concertId, Long userId) {
        Long nextPosition = queueTokenJpaRepository.findNextQueuePosition(concertId);
        QueueTokenJpaEntity entity = new QueueTokenJpaEntity(
                "qt_" + concertId + "_" + userId + "_" + nextPosition,
                userId,
                concertId,
                nextPosition,
                QueueTokenStatus.WAITING,
                LocalDateTime.now(),
                null,
                null
        );

        return queueTokenMapper.toDomain(queueTokenJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public QueueToken findByConcertIdAndToken(Long concertId, String token) {
        QueueTokenJpaEntity entity = queueTokenJpaRepository.findByConcertIdAndToken(concertId, token)
                .orElseThrow(() -> new IllegalArgumentException("대기열 토큰을 찾을 수 없습니다."));

        return queueTokenMapper.toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getWaitingPosition(Long concertId, String token) {
        return queueTokenJpaRepository.findByConcertIdAndToken(concertId, token)
                .map(QueueTokenJpaEntity::getQueuePosition)
                .orElseThrow(() -> new IllegalArgumentException("대기열 토큰을 찾을 수 없습니다."));
    }

    @Override
    public List<QueueToken> activateTopWaiting(Long concertId, int maxActiveCount) {
        return Collections.emptyList();
    }
}
