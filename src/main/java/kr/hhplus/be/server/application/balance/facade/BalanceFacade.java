package kr.hhplus.be.server.application.balance.facade;

import kr.hhplus.be.server.application.balance.dto.BalanceResult;
import kr.hhplus.be.server.application.balance.dto.ChargeBalanceCommand;
import kr.hhplus.be.server.application.balance.dto.GetBalanceQuery;
import kr.hhplus.be.server.application.balance.port.out.BalancePort;
import kr.hhplus.be.server.application.balance.usecase.ChargeBalanceUseCase;
import kr.hhplus.be.server.application.balance.usecase.GetBalanceUseCase;
import kr.hhplus.be.server.domain.balance.UserBalance;
import org.springframework.stereotype.Service;

@Service
public class BalanceFacade implements ChargeBalanceUseCase, GetBalanceUseCase {

    private final BalancePort balancePort;

    public BalanceFacade(BalancePort balancePort) {
        this.balancePort = balancePort;
    }

    @Override
    public BalanceResult charge(ChargeBalanceCommand command) {
        return toResult(balancePort.charge(command.userId(), command.amount()));
    }

    @Override
    public BalanceResult get(GetBalanceQuery query) {
        return toResult(balancePort.get(query.userId()));
    }

    private BalanceResult toResult(UserBalance balance) {
        return new BalanceResult(balance.userId(), balance.amount());
    }
}
