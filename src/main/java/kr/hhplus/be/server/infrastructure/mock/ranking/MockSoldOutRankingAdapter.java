package kr.hhplus.be.server.infrastructure.mock.ranking;

import kr.hhplus.be.server.application.ranking.port.out.SoldOutRankingPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Profile("mock")
public class MockSoldOutRankingAdapter implements SoldOutRankingPort {

    @Override
    public void recordSoldOutIfAbsent(Long scheduleId, long soldOutDurationSeconds) {
    }

    @Override
    public List<SoldOutRankingEntry> getTop(int limit) {
        return Collections.emptyList();
    }
}
