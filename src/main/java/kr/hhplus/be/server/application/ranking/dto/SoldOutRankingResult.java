package kr.hhplus.be.server.application.ranking.dto;

public record SoldOutRankingResult(
        int rank,
        Long scheduleId,
        long soldOutDurationSeconds
) {
}
