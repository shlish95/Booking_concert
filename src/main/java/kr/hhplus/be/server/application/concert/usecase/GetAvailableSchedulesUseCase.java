package kr.hhplus.be.server.application.concert.usecase;

import kr.hhplus.be.server.application.concert.dto.GetAvailableSchedulesQuery;
import kr.hhplus.be.server.application.concert.dto.ScheduleSummaryResult;

import java.util.List;

public interface GetAvailableSchedulesUseCase {

    List<ScheduleSummaryResult> get(GetAvailableSchedulesQuery query);
}
