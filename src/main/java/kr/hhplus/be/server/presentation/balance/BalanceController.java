package kr.hhplus.be.server.presentation.balance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.mock.MockApiService;
import kr.hhplus.be.server.presentation.balance.dto.BalanceChargeRequest;
import kr.hhplus.be.server.presentation.balance.dto.BalanceResponse;
import kr.hhplus.be.server.presentation.common.ApiResponse;
import kr.hhplus.be.server.presentation.common.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/balance")
@Tag(name = "Balance", description = "잔액 Mock API")
public class BalanceController {

    private final MockApiService mockApiService;

    public BalanceController(MockApiService mockApiService) {
        this.mockApiService = mockApiService;
    }

    @PostMapping("/charge")
    @Operation(
            summary = "잔액 충전",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mock 충전 성공",
                    content = @Content(schema = @Schema(implementation = BalanceApiResponse.class))
            )
    )
    public ResponseEntity<ApiResponse<BalanceResponse>> charge(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId,
            @RequestBody BalanceChargeRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(mockApiService.chargeBalance(userId, request.amount())));
    }

    @GetMapping
    @Operation(
            summary = "잔액 조회",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mock 조회 성공",
                    content = @Content(schema = @Schema(implementation = BalanceApiResponse.class))
            )
    )
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(mockApiService.getBalance(userId)));
    }

    private record BalanceApiResponse(boolean success, BalanceResponse data, ErrorResponse error) {
    }
}
