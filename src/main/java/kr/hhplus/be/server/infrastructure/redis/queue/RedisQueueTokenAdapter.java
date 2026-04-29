package kr.hhplus.be.server.infrastructure.redis.queue;

import kr.hhplus.be.server.application.queue.port.out.QueueTokenPort;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Primary
@Profile("!mock")
public class RedisQueueTokenAdapter implements QueueTokenPort {

    private static final String WAITING_KEY_FORMAT = "queue:%d:waiting";
    private static final String ACTIVE_KEY_FORMAT = "queue:%d:active";
    private static final String TOKEN_KEY_FORMAT = "queue:token:%s";

    private static final String FIELD_TOKEN = "token";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_CONCERT_ID = "concertId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_ISSUED_AT = "issuedAt";
    private static final String FIELD_ACTIVATED_AT = "activatedAt";
    private static final String FIELD_EXPIRED_AT = "expiredAt";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisQueueTokenAdapter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public QueueToken issue(Long concertId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        String token = generateToken(concertId, userId);
        stringRedisTemplate.opsForHash().putAll(
                tokenKey(token),
                Map.of(
                        FIELD_TOKEN, token,
                        FIELD_USER_ID, String.valueOf(userId),
                        FIELD_CONCERT_ID, String.valueOf(concertId),
                        FIELD_STATUS, QueueTokenStatus.WAITING.name(),
                        FIELD_ISSUED_AT, now.toString(),
                        FIELD_ACTIVATED_AT, "",
                        FIELD_EXPIRED_AT, ""
                )
        );
        stringRedisTemplate.opsForZSet().add(waitingKey(concertId), token, issuedAtScore(now));

        return findByConcertIdAndToken(concertId, token);
    }

    @Override
    public Long getWaitingPosition(Long concertId, String token) {
        Long rank = stringRedisTemplate.opsForZSet().rank(waitingKey(concertId), token);
        if (rank == null) {
            throw new IllegalArgumentException("대기 중인 토큰을 찾을 수 없습니다.");
        }
        return rank + 1;
    }

    @Override
    public List<QueueToken> activateTopWaiting(Long concertId, int maxActiveCount) {
        Long currentActiveCount = stringRedisTemplate.opsForSet().size(activeKey(concertId));
        long availableSlots = Math.max(0, maxActiveCount - (currentActiveCount == null ? 0 : currentActiveCount));
        if (availableSlots == 0) {
            return List.of();
        }

        List<String> waitingTokens = stringRedisTemplate.opsForZSet()
                .range(waitingKey(concertId), 0, availableSlots - 1)
                .stream()
                .toList();
        if (waitingTokens.isEmpty()) {
            return List.of();
        }

        LocalDateTime activatedAt = LocalDateTime.now();
        List<QueueToken> activatedTokens = new ArrayList<>();
        for (String token : waitingTokens) {
            stringRedisTemplate.opsForZSet().remove(waitingKey(concertId), token);
            stringRedisTemplate.opsForSet().add(activeKey(concertId), token);
            stringRedisTemplate.opsForHash().put(tokenKey(token), FIELD_STATUS, QueueTokenStatus.ACTIVE.name());
            stringRedisTemplate.opsForHash().put(tokenKey(token), FIELD_ACTIVATED_AT, activatedAt.toString());
            activatedTokens.add(findByConcertIdAndToken(concertId, token));
        }
        return activatedTokens;
    }

    @Override
    public QueueToken findByConcertIdAndToken(Long concertId, String token) {
        Map<Object, Object> metadata = stringRedisTemplate.opsForHash().entries(tokenKey(token));
        if (metadata.isEmpty()) {
            throw new IllegalArgumentException("대기열 토큰을 찾을 수 없습니다.");
        }

        Long storedConcertId = Long.parseLong(String.valueOf(metadata.get(FIELD_CONCERT_ID)));
        if (!storedConcertId.equals(concertId)) {
            throw new IllegalArgumentException("대기열 토큰을 찾을 수 없습니다.");
        }

        QueueTokenStatus status = resolveStatus(concertId, token, metadata);
        return new QueueToken(
                token,
                concertId,
                Long.parseLong(String.valueOf(metadata.get(FIELD_USER_ID))),
                resolveQueuePosition(concertId, token, status),
                status,
                LocalDateTime.parse(String.valueOf(metadata.get(FIELD_ISSUED_AT))),
                parseNullableDateTime(metadata.get(FIELD_ACTIVATED_AT)),
                parseNullableDateTime(metadata.get(FIELD_EXPIRED_AT))
        );
    }

    private QueueTokenStatus resolveStatus(Long concertId, String token, Map<Object, Object> metadata) {
        Boolean isActive = stringRedisTemplate.opsForSet().isMember(activeKey(concertId), token);
        if (Boolean.TRUE.equals(isActive)) {
            return QueueTokenStatus.ACTIVE;
        }

        Long waitingRank = stringRedisTemplate.opsForZSet().rank(waitingKey(concertId), token);
        if (waitingRank != null) {
            return QueueTokenStatus.WAITING;
        }

        return QueueTokenStatus.valueOf(String.valueOf(metadata.get(FIELD_STATUS)));
    }

    private Long resolveQueuePosition(Long concertId, String token, QueueTokenStatus status) {
        if (status == QueueTokenStatus.WAITING) {
            return getWaitingPosition(concertId, token);
        }
        return 0L;
    }

    private double issuedAtScore(LocalDateTime issuedAt) {
        return issuedAt.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private String generateToken(Long concertId, Long userId) {
        return "qt_" + concertId + "_" + userId + "_" + UUID.randomUUID();
    }

    private String waitingKey(Long concertId) {
        return WAITING_KEY_FORMAT.formatted(concertId);
    }

    private String activeKey(Long concertId) {
        return ACTIVE_KEY_FORMAT.formatted(concertId);
    }

    private String tokenKey(String token) {
        return TOKEN_KEY_FORMAT.formatted(token);
    }

    private LocalDateTime parseNullableDateTime(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        if (text.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(text);
    }
}
