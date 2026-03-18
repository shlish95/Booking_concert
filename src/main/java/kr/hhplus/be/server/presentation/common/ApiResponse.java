package kr.hhplus.be.server.presentation.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 API 응답")
public record ApiResponse<T>(
        @Schema(description = "성공 여부", example = "true")
        boolean success,
        @Schema(description = "응답 데이터")
        T data,
        @Schema(description = "에러 정보")
        ErrorResponse error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
}
