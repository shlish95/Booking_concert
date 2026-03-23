package kr.hhplus.be.server.application.balance.port.out;

import kr.hhplus.be.server.domain.balance.UserBalance;

public interface BalancePort {

    UserBalance charge(Long userId, Long amount);

    UserBalance get(Long userId);
}
