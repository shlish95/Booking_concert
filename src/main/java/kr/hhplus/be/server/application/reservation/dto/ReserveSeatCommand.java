package kr.hhplus.be.server.application.reservation.dto;

public record ReserveSeatCommand(
        String queueToken,
        Long userId,
        Long scheduleId,
        Integer seatNumber
) {
}
