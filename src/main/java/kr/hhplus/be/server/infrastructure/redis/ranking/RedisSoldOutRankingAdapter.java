package kr.hhplus.be.server.infrastructure.redis.ranking;

import kr.hhplus.be.server.application.ranking.port.out.SoldOutRankingPort;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@Profile("!mock")
public class RedisSoldOutRankingAdapter implements SoldOutRankingPort {

    static final String SOLD_OUT_RANKING_KEY = "ranking:concert:soldout:speed:all";
    private static final String MEMBER_PREFIX = "schedule:";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisSoldOutRankingAdapter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void recordSoldOutIfAbsent(Long scheduleId, long soldOutDurationSeconds) {
        stringRedisTemplate.opsForZSet().addIfAbsent(
                SOLD_OUT_RANKING_KEY,
                member(scheduleId),
                soldOutDurationSeconds
        );
    }

    @Override
    public List<SoldOutRankingEntry> getTop(int limit) {
        Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                .rangeWithScores(SOLD_OUT_RANKING_KEY, 0, Math.max(0, limit - 1L));
        if (tuples == null) {
            return Collections.emptyList();
        }

        return tuples.stream()
                .map(this::toEntry)
                .toList();
    }

    private String member(Long scheduleId) {
        return MEMBER_PREFIX + scheduleId;
    }

    private SoldOutRankingEntry toEntry(ZSetOperations.TypedTuple<String> tuple) {
        String value = tuple.getValue();
        if (value == null || !value.startsWith(MEMBER_PREFIX)) {
            throw new IllegalStateException("잘못된 랭킹 member 형식입니다. member=" + value);
        }
        Double score = tuple.getScore();
        if (score == null) {
            throw new IllegalStateException("랭킹 score가 없습니다. member=" + value);
        }
        return new SoldOutRankingEntry(
                Long.parseLong(value.substring(MEMBER_PREFIX.length())),
                score.longValue()
        );
    }
}
