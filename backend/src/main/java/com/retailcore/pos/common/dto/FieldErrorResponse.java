package com.retailcore.pos.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Field validation error response.")
public record FieldErrorResponse(String field, String message) {
}
