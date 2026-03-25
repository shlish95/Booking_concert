package kr.hhplus.be.server.infrastructure.persistence.adapter;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.domain.balance.UserBalance;
import kr.hhplus.be.server.infrastructure.persistence.entity.UserJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.UserBalanceMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({
        TestcontainersConfiguration.class,
        BalancePersistenceAdapter.class,
        UserBalanceMapper.class
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BalancePersistenceAdapterTest {

    @Autowired
    private BalancePersistenceAdapter balancePersistenceAdapter;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    @DisplayName("사용자 현재 잔액을 조회한다")
    void getBalance() {
        UserJpaEntity savedUser = userJpaRepository.save(new UserJpaEntity("user-uuid-1", 10_000L));

        UserBalance balance = balancePersistenceAdapter.get(savedUser.getId());

        assertThat(balance.userId()).isEqualTo(savedUser.getId());
        assertThat(balance.amount()).isEqualTo(10_000L);
        assertThat(balance.version()).isZero();
    }

    @Test
    @DisplayName("사용자 잔액을 충전하고 반영된 잔액을 반환한다")
    void chargeBalance() {
        UserJpaEntity savedUser = userJpaRepository.save(new UserJpaEntity("user-uuid-2", 10_000L));

        UserBalance result = balancePersistenceAdapter.charge(savedUser.getId(), 5_000L);

        assertThat(result.userId()).isEqualTo(savedUser.getId());
        assertThat(result.amount()).isEqualTo(15_000L);
        assertThat(userJpaRepository.findById(savedUser.getId()))
                .get()
                .extracting(UserJpaEntity::getBalance)
                .isEqualTo(15_000L);
    }
}
