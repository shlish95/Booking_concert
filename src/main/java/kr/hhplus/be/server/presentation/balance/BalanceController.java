package kr.hhplus.be.server.presentation.balance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.balance.dto.BalanceResult;
import kr.hhplus.be.server.application.balance.dto.ChargeBalanceCommand;
import kr.hhplus.be.server.application.balance.dto.GetBalanceQuery;
import kr.hhplus.be.server.application.balance.usecase.ChargeBalanceUseCase;
import kr.hhplus.be.server.application.balance.usecase.GetBalanceUseCase;
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

    private final ChargeBalanceUseCase chargeBalanceUseCase;
    private final GetBalanceUseCase getBalanceUseCase;

    public BalanceController(
            ChargeBalanceUseCase chargeBalanceUseCase,
            GetBalanceUseCase getBalanceUseCase
    ) {
        this.chargeBalanceUseCase = chargeBalanceUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
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
        BalanceResult result = chargeBalanceUseCase.charge(new ChargeBalanceCommand(userId, request.amount()));
        return ResponseEntity.ok(ApiResponse.success(toResponse(result)));
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
        BalanceResult result = getBalanceUseCase.get(new GetBalanceQuery(userId));
        return ResponseEntity.ok(ApiResponse.success(toResponse(result)));
    }

    private BalanceResponse toResponse(BalanceResult result) {
        return new BalanceResponse(result.userId(), result.balance());
    }

    private record BalanceApiResponse(boolean success, BalanceResponse data, ErrorResponse error) {
    }
}
