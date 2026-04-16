package kr.hhplus.be.server.infrastructure.redis.reservation;

import kr.hhplus.be.server.application.reservation.port.out.UserReservationLockPort;
import kr.hhplus.be.server.domain.reservation.ConcurrentReservationRequestException;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

@Component
@Profile("!mock")
public class RedisUserReservationLockAdapter implements UserReservationLockPort {

    private static final Duration LOCK_TTL = Duration.ofSeconds(5);
    private static final String LOCK_KEY_PREFIX = "reservation:hold:user:";

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            end
            return 0
            """,
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    public RedisUserReservationLockAdapter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public <T> T executeWithUserLock(Long userId, Supplier<T> action) {
        String lockKey = LOCK_KEY_PREFIX + userId;
        String ownerToken = UUID.randomUUID().toString();

        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, ownerToken, LOCK_TTL);

        if (!Boolean.TRUE.equals(acquired)) {
            throw new ConcurrentReservationRequestException();
        }

        try {
            return action.get();
        } finally {
            stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), ownerToken);
        }
    }
}
