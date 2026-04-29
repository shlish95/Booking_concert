package kr.hhplus.be.server.application.queue.facade;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.application.queue.dto.GetQueueTokenQuery;
import kr.hhplus.be.server.application.queue.dto.IssueQueueTokenCommand;
import kr.hhplus.be.server.application.queue.dto.QueueTokenResult;
import kr.hhplus.be.server.domain.queue.QueueTokenStatus;
import kr.hhplus.be.server.infrastructure.redis.queue.RedisQueueTokenAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({
        TestcontainersConfiguration.class,
        QueueTokenFacade.class,
        RedisQueueTokenAdapter.class
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QueueTokenFacadeRedisTest {

    @Autowired
    private QueueTokenFacade queueTokenFacade;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    @DisplayName("대기열 토큰 발급 시 waiting에 삽입되고 순번이 증가한다")
    void issue_waitingInsertAndRank() {
        QueueTokenResult first = queueTokenFacade.issue(new IssueQueueTokenCommand(10L, 1L));
        QueueTokenResult second = queueTokenFacade.issue(new IssueQueueTokenCommand(10L, 2L));
        QueueTokenResult third = queueTokenFacade.issue(new IssueQueueTokenCommand(10L, 3L));

        assertThat(first.status()).isEqualTo(QueueTokenStatus.WAITING.name());
        assertThat(first.queuePosition()).isEqualTo(1L);
        assertThat(second.queuePosition()).isEqualTo(2L);
        assertThat(third.queuePosition()).isEqualTo(3L);
    }

    @Test
    @DisplayName("상위 N명의 waiting 토큰이 active로 전환된다")
    void activateTopWaiting() {
        QueueTokenResult first = queueTokenFacade.issue(new IssueQueueTokenCommand(20L, 1L));
        QueueTokenResult second = queueTokenFacade.issue(new IssueQueueTokenCommand(20L, 2L));
        QueueTokenResult third = queueTokenFacade.issue(new IssueQueueTokenCommand(20L, 3L));

        List<QueueTokenResult> activated = queueTokenFacade.activateTopWaiting(20L, 2);

        assertThat(activated).hasSize(2);
        assertThat(activated)
                .extracting(QueueTokenResult::token)
                .containsExactly(first.token(), second.token());
        assertThat(queueTokenFacade.get(new GetQueueTokenQuery(20L, first.token())).status())
                .isEqualTo(QueueTokenStatus.ACTIVE.name());
        assertThat(queueTokenFacade.get(new GetQueueTokenQuery(20L, second.token())).status())
                .isEqualTo(QueueTokenStatus.ACTIVE.name());
        assertThat(queueTokenFacade.get(new GetQueueTokenQuery(20L, third.token())).status())
                .isEqualTo(QueueTokenStatus.WAITING.name());
        assertThat(queueTokenFacade.get(new GetQueueTokenQuery(20L, third.token())).queuePosition())
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("ACTIVE 토큰은 같은 concertId에서만 검증 성공한다")
    void get_activeValidationAndDifferentConcertFailure() {
        QueueTokenResult issued = queueTokenFacade.issue(new IssueQueueTokenCommand(30L, 1L));
        queueTokenFacade.activateTopWaiting(30L, 1);

        QueueTokenResult active = queueTokenFacade.get(new GetQueueTokenQuery(30L, issued.token()));

        assertThat(active.status()).isEqualTo(QueueTokenStatus.ACTIVE.name());
        assertThatThrownBy(() -> queueTokenFacade.get(new GetQueueTokenQuery(31L, issued.token())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 active가 가득 차 있으면 추가 활성화되지 않는다")
    void activateTopWaiting_respectsActiveLimit() {
        QueueTokenResult first = queueTokenFacade.issue(new IssueQueueTokenCommand(40L, 1L));
        queueTokenFacade.issue(new IssueQueueTokenCommand(40L, 2L));

        List<QueueTokenResult> firstActivation = queueTokenFacade.activateTopWaiting(40L, 1);
        List<QueueTokenResult> secondActivation = queueTokenFacade.activateTopWaiting(40L, 1);

        assertThat(firstActivation).hasSize(1);
        assertThat(firstActivation.get(0).token()).isEqualTo(first.token());
        assertThat(secondActivation).isEmpty();
    }
}
