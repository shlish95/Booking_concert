package kr.hhplus.be.server.application.concert.usecase;

import kr.hhplus.be.server.application.concert.dto.AvailableSeatsResult;
import kr.hhplus.be.server.application.concert.dto.GetAvailableSeatsQuery;

public interface GetAvailableSeatsUseCase {

    AvailableSeatsResult get(GetAvailableSeatsQuery query);
}
