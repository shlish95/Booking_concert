package kr.hhplus.be.server.application.ranking.port.out;

import java.util.List;

public interface SoldOutRankingPort {

    void recordSoldOutIfAbsent(Long scheduleId, long soldOutDurationSeconds);

    List<SoldOutRankingEntry> getTop(int limit);

    record SoldOutRankingEntry(
            Long scheduleId,
            long soldOutDurationSeconds
    ) {
    }
}
