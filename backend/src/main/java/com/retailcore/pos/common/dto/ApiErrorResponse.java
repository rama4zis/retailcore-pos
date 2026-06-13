package com.retailcore.pos.common.dto;

import java.time.Instant;
import java.util.List;

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
