package kr.hhplus.be.server.domain.concert;

import java.time.LocalDate;

public record ConcertSchedule(
        Long scheduleId,
        Long concertId,
        LocalDate concertDate
) {
}
