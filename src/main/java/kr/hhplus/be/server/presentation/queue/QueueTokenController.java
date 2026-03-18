package kr.hhplus.be.server.presentation.queue;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.mock.MockApiService;
import kr.hhplus.be.server.presentation.common.ApiResponse;
import kr.hhplus.be.server.presentation.common.ErrorResponse;
import kr.hhplus.be.server.presentation.queue.dto.QueueTokenIssueRequest;
import kr.hhplus.be.server.presentation.queue.dto.QueueTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/concerts/{concertId}/queue-tokens")
@Tag(name = "Queue Token", description = "콘서트별 대기열 토큰 Mock API")
public class QueueTokenController {

    private final MockApiService mockApiService;

    public QueueTokenController(MockApiService mockApiService) {
        this.mockApiService = mockApiService;
    }

    @PostMapping
    @Operation(
            summary = "대기열 토큰 발급",
            description = "실제 대기열 처리 없이 콘서트별 대기열 토큰 응답 형태를 제공한다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Mock 토큰 발급 성공",
                    content = @Content(schema = @Schema(implementation = QueueTokenIssueApiResponse.class))
            )
    )
    public ResponseEntity<ApiResponse<QueueTokenResponse>> issueToken(
            @Parameter(description = "콘서트 ID", example = "10") @PathVariable Long concertId,
            @RequestBody QueueTokenIssueRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mockApiService.issueQueueToken(concertId, request.userId())));
    }

    @GetMapping("/{token}")
    @Operation(
            summary = "대기번호 조회",
            description = "실제 상태 전이 없이 대기열 조회 응답 형태를 제공한다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mock 대기번호 조회 성공",
                    content = @Content(schema = @Schema(implementation = QueueTokenIssueApiResponse.class))
            )
    )
    public ResponseEntity<ApiResponse<QueueTokenResponse>> getToken(
            @Parameter(description = "콘서트 ID", example = "10") @PathVariable Long concertId,
            @Parameter(description = "토큰 값", example = "qt_mock_10_1") @PathVariable String token
    ) {
        return ResponseEntity.ok(ApiResponse.success(mockApiService.getQueueToken(concertId, token)));
    }

    private record QueueTokenIssueApiResponse(boolean success, QueueTokenResponse data, ErrorResponse error) {
    }
}
