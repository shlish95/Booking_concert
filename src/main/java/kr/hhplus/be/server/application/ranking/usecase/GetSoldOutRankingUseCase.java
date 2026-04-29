package kr.hhplus.be.server.application.ranking.usecase;

import kr.hhplus.be.server.application.ranking.dto.SoldOutRankingResult;

import java.util.List;

public interface GetSoldOutRankingUseCase {

    List<SoldOutRankingResult> getTop(int limit);
}
