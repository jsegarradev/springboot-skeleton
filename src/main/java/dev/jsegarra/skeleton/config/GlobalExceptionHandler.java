package dev.jsegarra.skeleton.config;

import dev.jsegarra.skeleton.adapter.in.contracts.response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Shared error handling for the whole HTTP surface (springboot.md §3). Maps uncaught exceptions to the standard
 * {@link ApiError} body; feature slices add specific {@code @ExceptionHandler}s (e.g. domain exceptions → 400/404) as
 * they are introduced.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String UNEXPECTED_CODE = "INTERNAL_ERROR";
    private static final String UNEXPECTED_MESSAGE = "Unexpected error";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(final Exception exception) {
        final ApiError body = new ApiError(UNEXPECTED_CODE, UNEXPECTED_MESSAGE);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
