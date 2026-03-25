package kr.hhplus.be.server.infrastructure.persistence.mapper;

import kr.hhplus.be.server.domain.balance.UserBalance;
import kr.hhplus.be.server.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserBalanceMapper {

    public UserBalance toDomain(UserJpaEntity entity) {
        return new UserBalance(
                entity.getId(),
                entity.getBalance(),
                entity.getVersion()
        );
    }
}
