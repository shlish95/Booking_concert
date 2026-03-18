package kr.hhplus.be.server.presentation.reservation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.mock.MockApiService;
import kr.hhplus.be.server.presentation.common.ApiResponse;
import kr.hhplus.be.server.presentation.common.ErrorResponse;
import kr.hhplus.be.server.presentation.reservation.dto.ReservationCreateRequest;
import kr.hhplus.be.server.presentation.reservation.dto.ReservationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservation", description = "좌석 예약 Mock API")
public class ReservationController {

    private final MockApiService mockApiService;

    public ReservationController(MockApiService mockApiService) {
        this.mockApiService = mockApiService;
    }

    @PostMapping
    @Operation(
            summary = "좌석 예약 요청",
            description = "실제 선점 로직 없이 예약 성공 응답 구조를 제공한다. `X-Queue-Token` 헤더는 문서화 목적으로만 사용한다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Mock 예약 성공",
                    content = @Content(schema = @Schema(implementation = ReservationApiResponse.class))
            )
    )
    public ResponseEntity<ApiResponse<ReservationResponse>> reserveSeat(
            @Parameter(description = "대기열 토큰", example = "qt_mock_10_1") @RequestHeader("X-Queue-Token") String queueToken,
            @RequestBody ReservationCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        mockApiService.createReservation(request.userId(), request.scheduleId(), request.seatNumber())
                ));
    }

    private record ReservationApiResponse(boolean success, ReservationResponse data, ErrorResponse error) {
    }
}
