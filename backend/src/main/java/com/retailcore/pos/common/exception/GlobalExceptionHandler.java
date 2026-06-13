package com.retailcore.pos.common.exception;

import com.retailcore.pos.category.exception.CategoryInUseException;
import com.retailcore.pos.common.dto.ApiErrorResponse;
import com.retailcore.pos.inventory.InvalidStockAdjustmentException;
import com.retailcore.pos.inventory.NegativeStockException;
import com.retailcore.pos.common.dto.FieldErrorResponse;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateResource(DuplicateResourceException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CategoryInUseException.class)
    ResponseEntity<ApiErrorResponse> handleCategoryInUse(CategoryInUseException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler({NegativeStockException.class, InvalidStockAdjustmentException.class})
    ResponseEntity<ApiErrorResponse> handleInventoryConflict(RuntimeException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<FieldErrorResponse> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldErrorResponse::field))
                .toList();

        return ResponseEntity.badRequest().body(ApiErrorResponse.validationError(fieldErrors));
    }

    private static ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(status.value(), status.getReasonPhrase(), message));
    }
}
