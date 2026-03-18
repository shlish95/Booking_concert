package kr.hhplus.be.server.presentation.concert;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.mock.MockApiService;
import kr.hhplus.be.server.presentation.common.ApiResponse;
import kr.hhplus.be.server.presentation.common.ErrorResponse;
import kr.hhplus.be.server.presentation.concert.dto.AvailableSeatsResponse;
import kr.hhplus.be.server.presentation.concert.dto.ScheduleSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Concert Query", description = "예약 가능 날짜 및 좌석 Mock API")
public class ConcertQueryController {

    private final MockApiService mockApiService;

    public ConcertQueryController(MockApiService mockApiService) {
        this.mockApiService = mockApiService;
    }

    @GetMapping("/api/v1/concerts/{concertId}/schedules")
    @Operation(
            summary = "예약 가능 날짜 조회",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mock 회차 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = SchedulesApiResponse.class))
            )
    )
    public ResponseEntity<ApiResponse<List<ScheduleSummaryResponse>>> getSchedules(
            @Parameter(description = "콘서트 ID", example = "10") @PathVariable Long concertId
    ) {
        return ResponseEntity.ok(ApiResponse.success(mockApiService.getSchedules(concertId)));
    }

    @GetMapping("/api/v1/schedules/{scheduleId}/seats/available")
    @Operation(
            summary = "예약 가능 좌석 조회",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mock 좌석 조회 성공",
                    content = @Content(schema = @Schema(implementation = AvailableSeatsApiResponse.class))
            )
    )
    public ResponseEntity<ApiResponse<AvailableSeatsResponse>> getAvailableSeats(
            @Parameter(description = "회차 ID", example = "100") @PathVariable Long scheduleId
    ) {
        return ResponseEntity.ok(ApiResponse.success(mockApiService.getAvailableSeats(scheduleId)));
    }

    private record SchedulesApiResponse(
            boolean success,
            @ArraySchema(schema = @Schema(implementation = ScheduleSummaryResponse.class))
            List<ScheduleSummaryResponse> data,
            ErrorResponse error
    ) {
    }

    private record AvailableSeatsApiResponse(boolean success, AvailableSeatsResponse data, ErrorResponse error) {
    }
}
