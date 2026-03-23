package kr.hhplus.be.server.application.concert.dto;

import java.time.LocalDate;

public record ScheduleSummaryResult(
        Long scheduleId,
        Long concertId,
        LocalDate concertDate
) {
}
