package kr.hhplus.be.server.infrastructure.persistence.adapter;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenStatus;
import kr.hhplus.be.server.infrastructure.persistence.entity.QueueTokenJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.QueueTokenMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.QueueTokenJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({
        TestcontainersConfiguration.class,
        QueueTokenPersistenceAdapter.class,
        QueueTokenMapper.class
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QueueTokenPersistenceAdapterTest {

    @Autowired
    private QueueTokenPersistenceAdapter queueTokenPersistenceAdapter;

    @Autowired
    private QueueTokenJpaRepository queueTokenJpaRepository;

    @Test
    @DisplayName("콘서트별 대기열 토큰을 발급한다")
    void issueQueueToken() {
        queueTokenJpaRepository.save(new QueueTokenJpaEntity(
                "qt_10_1_1",
                1L,
                10L,
                1L,
                QueueTokenStatus.WAITING,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                null,
                null
        ));

        QueueToken issued = queueTokenPersistenceAdapter.issue(10L, 2L);

        assertThat(issued.concertId()).isEqualTo(10L);
        assertThat(issued.userId()).isEqualTo(2L);
        assertThat(issued.queuePosition()).isEqualTo(2L);
        assertThat(issued.status()).isEqualTo(QueueTokenStatus.WAITING);
        assertThat(issued.token()).startsWith("qt_10_2_2");
    }

    @Test
    @DisplayName("콘서트와 토큰 값으로 대기열 토큰을 조회한다")
    void findByConcertIdAndToken() {
        QueueTokenJpaEntity saved = queueTokenJpaRepository.save(new QueueTokenJpaEntity(
                "qt_20_3_1",
                3L,
                20L,
                1L,
                QueueTokenStatus.ACTIVE,
                LocalDateTime.of(2026, 3, 25, 9, 0, 0),
                LocalDateTime.of(2026, 3, 25, 9, 1, 0),
                null
        ));

        QueueToken result = queueTokenPersistenceAdapter.findByConcertIdAndToken(20L, saved.getToken());

        assertThat(result.token()).isEqualTo("qt_20_3_1");
        assertThat(result.concertId()).isEqualTo(20L);
        assertThat(result.userId()).isEqualTo(3L);
        assertThat(result.queuePosition()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(QueueTokenStatus.ACTIVE);
    }
}
