package kr.hhplus.be.server.application.ranking.facade;

import kr.hhplus.be.server.application.ranking.dto.SoldOutRankingResult;
import kr.hhplus.be.server.application.ranking.port.out.SoldOutRankingPort;
import kr.hhplus.be.server.application.ranking.usecase.GetSoldOutRankingUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SoldOutRankingFacade implements GetSoldOutRankingUseCase {

    private final SoldOutRankingPort soldOutRankingPort;

    public SoldOutRankingFacade(SoldOutRankingPort soldOutRankingPort) {
        this.soldOutRankingPort = soldOutRankingPort;
    }

    @Override
    public List<SoldOutRankingResult> getTop(int limit) {
        AtomicInteger rank = new AtomicInteger(1);
        return soldOutRankingPort.getTop(limit).stream()
                .map(entry -> new SoldOutRankingResult(
                        rank.getAndIncrement(),
                        entry.scheduleId(),
                        entry.soldOutDurationSeconds()
                ))
                .toList();
    }
}
