package kr.hhplus.be.server.application.concert.port.out;

import kr.hhplus.be.server.domain.concert.ConcertSchedule;

import java.util.List;

public interface ConcertQueryPort {

    List<ConcertSchedule> getSchedules(Long concertId);

    ConcertSchedule getSchedule(Long scheduleId);

    List<Integer> getAvailableSeatNumbers(Long scheduleId);
}
