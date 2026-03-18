package kr.hhplus.be.server.presentation.queue.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대기열 토큰 발급 요청")
public record QueueTokenIssueRequest(
        @Schema(description = "사용자 ID", example = "1")
        Long userId
) {
}
