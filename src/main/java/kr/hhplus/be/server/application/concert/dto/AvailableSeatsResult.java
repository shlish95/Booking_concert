package kr.hhplus.be.server.application.concert.dto;

import java.util.List;

public record AvailableSeatsResult(
        Long scheduleId,
        List<Integer> availableSeats
) {
}
