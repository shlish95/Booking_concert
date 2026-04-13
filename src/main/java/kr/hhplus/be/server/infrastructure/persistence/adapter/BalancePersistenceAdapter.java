package kr.hhplus.be.server.infrastructure.persistence.adapter;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.application.balance.port.out.BalancePort;
import kr.hhplus.be.server.domain.balance.InsufficientBalanceException;
import kr.hhplus.be.server.domain.balance.OptimisticLockConflictException;
import kr.hhplus.be.server.domain.balance.UserBalance;
import kr.hhplus.be.server.infrastructure.persistence.entity.UserJpaEntity;
import kr.hhplus.be.server.infrastructure.persistence.mapper.UserBalanceMapper;
import kr.hhplus.be.server.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@Component
@Profile("!mock")
public class BalancePersistenceAdapter implements BalancePort {

    private final UserJpaRepository userJpaRepository;
    private final UserBalanceMapper userBalanceMapper;

    public BalancePersistenceAdapter(UserJpaRepository userJpaRepository, UserBalanceMapper userBalanceMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userBalanceMapper = userBalanceMapper;
    }

    @Override
    @Transactional
    public UserBalance charge(Long userId, Long amount) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        Long updatedBalance = user.getBalance() + amount;
        user.setBalance(updatedBalance);
        return userBalanceMapper.toDomain(user);
    }

    @Override
    @Transactional
    public UserBalance use(Long userId, Long amount) {
        try {
            UserJpaEntity user = userJpaRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

            if (user.getBalance() < amount) {
                throw new InsufficientBalanceException();
            }

            user.setBalance(user.getBalance() - amount);
            UserJpaEntity saved = userJpaRepository.saveAndFlush(user);
            return userBalanceMapper.toDomain(saved);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new OptimisticLockConflictException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserBalance get(Long userId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        return userBalanceMapper.toDomain(user);
    }
}
