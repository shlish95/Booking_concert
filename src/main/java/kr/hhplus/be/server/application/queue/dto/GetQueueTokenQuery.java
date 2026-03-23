package kr.hhplus.be.server.application.queue.dto;

public record GetQueueTokenQuery(
        Long concertId,
        String token
) {
}
