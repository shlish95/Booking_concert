package kr.hhplus.be.server.presentation.concert;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.concert.dto.AvailableSeatsResult;
import kr.hhplus.be.server.application.concert.dto.GetAvailableSchedulesQuery;
import kr.hhplus.be.server.application.concert.dto.GetAvailableSeatsQuery;
import kr.hhplus.be.server.application.concert.dto.ScheduleSummaryResult;
import kr.hhplus.be.server.application.concert.usecase.GetAvailableSchedulesUseCase;
import kr.hhplus.be.server.application.concert.usecase.GetAvailableSeatsUseCase;
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

    private final GetAvailableSchedulesUseCase getAvailableSchedulesUseCase;
    private final GetAvailableSeatsUseCase getAvailableSeatsUseCase;

    public ConcertQueryController(
            GetAvailableSchedulesUseCase getAvailableSchedulesUseCase,
            GetAvailableSeatsUseCase getAvailableSeatsUseCase
    ) {
        this.getAvailableSchedulesUseCase = getAvailableSchedulesUseCase;
        this.getAvailableSeatsUseCase = getAvailableSeatsUseCase;
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
        List<ScheduleSummaryResponse> response = getAvailableSchedulesUseCase.get(
                        new GetAvailableSchedulesQuery(concertId)
                ).stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
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
        AvailableSeatsResult result = getAvailableSeatsUseCase.get(new GetAvailableSeatsQuery(scheduleId));
        return ResponseEntity.ok(ApiResponse.success(toResponse(result)));
    }

    private ScheduleSummaryResponse toResponse(ScheduleSummaryResult result) {
        return new ScheduleSummaryResponse(result.scheduleId(), result.concertId(), result.concertDate());
    }

    private AvailableSeatsResponse toResponse(AvailableSeatsResult result) {
        return new AvailableSeatsResponse(result.scheduleId(), result.availableSeats());
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
