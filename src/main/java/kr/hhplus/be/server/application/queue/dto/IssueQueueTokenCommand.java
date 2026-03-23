package kr.hhplus.be.server.application.queue.dto;

public record IssueQueueTokenCommand(
        Long concertId,
        Long userId
) {
}
