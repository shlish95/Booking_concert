package kr.hhplus.be.server.presentation.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.payment.dto.PayReservationCommand;
import kr.hhplus.be.server.application.payment.dto.PaymentResult;
import kr.hhplus.be.server.application.payment.usecase.PayReservationUseCase;
import kr.hhplus.be.server.presentation.common.ApiResponse;
import kr.hhplus.be.server.presentation.common.ErrorResponse;
import kr.hhplus.be.server.presentation.payment.dto.PaymentCreateRequest;
import kr.hhplus.be.server.presentation.payment.dto.PaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment", description = "결제 Mock API")
public class PaymentController {

    private final PayReservationUseCase payReservationUseCase;

    public PaymentController(PayReservationUseCase payReservationUseCase) {
        this.payReservationUseCase = payReservationUseCase;
    }

    @PostMapping
    @Operation(
            summary = "결제 요청",
            description = "실제 결제 로직 없이 결제 성공 응답 구조를 제공한다. `X-Queue-Token` 헤더는 문서화 목적으로만 사용한다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Mock 결제 성공",
                    content = @Content(schema = @Schema(implementation = PaymentApiResponse.class))
            )
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Parameter(description = "대기열 토큰", example = "qt_mock_10_1") @RequestHeader("X-Queue-Token") String queueToken,
            @RequestBody PaymentCreateRequest request
    ) {
        PaymentResult result = payReservationUseCase.pay(new PayReservationCommand(
                queueToken,
                request.userId(),
                request.reservationId()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toResponse(result)));
    }

    private PaymentResponse toResponse(PaymentResult result) {
        return new PaymentResponse(
                result.paymentId(),
                result.reservationId(),
                result.userId(),
                result.amount(),
                result.status(),
                result.paidAt()
        );
    }

    private record PaymentApiResponse(boolean success, PaymentResponse data, ErrorResponse error) {
    }
}
