package kr.hhplus.be.server.infrastructure.mock.balance;

import kr.hhplus.be.server.application.balance.port.out.BalancePort;
import kr.hhplus.be.server.domain.balance.UserBalance;
import org.springframework.stereotype.Component;

@Component
public class MockBalanceAdapter implements BalancePort {

    @Override
    public UserBalance charge(Long userId, Long amount) {
        return new UserBalance(userId, 50_000L + amount, 0L);
    }

    @Override
    public UserBalance get(Long userId) {
        return new UserBalance(userId, 150_000L, 0L);
    }
}
