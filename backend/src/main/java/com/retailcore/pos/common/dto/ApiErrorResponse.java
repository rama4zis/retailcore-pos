package com.retailcore.pos.common.dto;

import java.time.Instant;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API error response.")
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<FieldErrorResponse> fieldErrors
) {
    public static ApiErrorResponse of(int status, String error, String message) {
        return new ApiErrorResponse(Instant.now(), status, error, message, List.of());
    }

    public static ApiErrorResponse validationError(List<FieldErrorResponse> fieldErrors) {
        return new ApiErrorResponse(Instant.now(), 400, "Bad Request", "Validation failed", fieldErrors);
    }
}
