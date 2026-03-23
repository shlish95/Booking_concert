package kr.hhplus.be.server.application.concert.facade;

import kr.hhplus.be.server.application.concert.dto.AvailableSeatsResult;
import kr.hhplus.be.server.application.concert.dto.GetAvailableSchedulesQuery;
import kr.hhplus.be.server.application.concert.dto.GetAvailableSeatsQuery;
import kr.hhplus.be.server.application.concert.dto.ScheduleSummaryResult;
import kr.hhplus.be.server.application.concert.port.out.ConcertQueryPort;
import kr.hhplus.be.server.application.concert.usecase.GetAvailableSchedulesUseCase;
import kr.hhplus.be.server.application.concert.usecase.GetAvailableSeatsUseCase;
import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConcertQueryFacade implements GetAvailableSchedulesUseCase, GetAvailableSeatsUseCase {

    private final ConcertQueryPort concertQueryPort;

    public ConcertQueryFacade(ConcertQueryPort concertQueryPort) {
        this.concertQueryPort = concertQueryPort;
    }

    @Override
    public List<ScheduleSummaryResult> get(GetAvailableSchedulesQuery query) {
        return concertQueryPort.getSchedules(query.concertId()).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public AvailableSeatsResult get(GetAvailableSeatsQuery query) {
        return new AvailableSeatsResult(
                query.scheduleId(),
                concertQueryPort.getAvailableSeatNumbers(query.scheduleId())
        );
    }

    private ScheduleSummaryResult toResult(ConcertSchedule schedule) {
        return new ScheduleSummaryResult(
                schedule.scheduleId(),
                schedule.concertId(),
                schedule.concertDate()
        );
    }
}
