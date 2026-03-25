package kr.hhplus.be.server.infrastructure.persistence.mapper;

import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.infrastructure.persistence.entity.QueueTokenJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class QueueTokenMapper {

    public QueueToken toDomain(QueueTokenJpaEntity entity) {
        return new QueueToken(
                entity.getToken(),
                entity.getConcertId(),
                entity.getUserId(),
                entity.getQueuePosition(),
                entity.getStatus(),
                entity.getIssuedAt(),
                entity.getActivatedAt(),
                entity.getExpiredAt()
        );
    }
}
