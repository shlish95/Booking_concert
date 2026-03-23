package kr.hhplus.be.server.application.balance.usecase;

import kr.hhplus.be.server.application.balance.dto.BalanceResult;
import kr.hhplus.be.server.application.balance.dto.GetBalanceQuery;

public interface GetBalanceUseCase {

    BalanceResult get(GetBalanceQuery query);
}
