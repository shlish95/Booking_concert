package kr.hhplus.be.server.application.balance.usecase;

import kr.hhplus.be.server.application.balance.dto.BalanceResult;
import kr.hhplus.be.server.application.balance.dto.ChargeBalanceCommand;

public interface ChargeBalanceUseCase {

    BalanceResult charge(ChargeBalanceCommand command);
}
