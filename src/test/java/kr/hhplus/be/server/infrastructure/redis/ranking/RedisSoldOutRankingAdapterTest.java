package kr.hhplus.be.server.infrastructure.redis.ranking;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.application.ranking.facade.SoldOutRankingFacade;
import kr.hhplus.be.server.application.ranking.port.out.SoldOutRankingPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({
        TestcontainersConfiguration.class,
        RedisSoldOutRankingAdapter.class,
        SoldOutRankingFacade.class
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RedisSoldOutRankingAdapterTest {

    @Autowired
    private SoldOutRankingPort soldOutRankingPort;

    @Autowired
    private SoldOutRankingFacade soldOutRankingFacade;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    @DisplayName("빠른 매진 랭킹은 score 오름차순 기준으로 Top N을 조회한다")
    void getTopRanking() {
        soldOutRankingPort.recordSoldOutIfAbsent(300L, 120L);
        soldOutRankingPort.recordSoldOutIfAbsent(100L, 30L);
        soldOutRankingPort.recordSoldOutIfAbsent(200L, 60L);

        assertThat(soldOutRankingFacade.getTop(2))
                .extracting("rank", "scheduleId", "soldOutDurationSeconds")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1, 100L, 30L),
                        org.assertj.core.groups.Tuple.tuple(2, 200L, 60L)
                );
    }

    @Test
    @DisplayName("같은 schedule의 매진 랭킹은 중복 적재되지 않는다")
    void recordSoldOutIfAbsent() {
        soldOutRankingPort.recordSoldOutIfAbsent(10L, 50L);
        soldOutRankingPort.recordSoldOutIfAbsent(10L, 90L);

        assertThat(stringRedisTemplate.opsForZSet().zCard(RedisSoldOutRankingAdapter.SOLD_OUT_RANKING_KEY))
                .isEqualTo(1);
        assertThat(soldOutRankingFacade.getTop(10))
                .extracting("scheduleId", "soldOutDurationSeconds")
                .containsExactly(org.assertj.core.groups.Tuple.tuple(10L, 50L));
    }
}
