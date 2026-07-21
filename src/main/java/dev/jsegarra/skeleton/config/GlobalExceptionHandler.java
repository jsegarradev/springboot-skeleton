package dev.jsegarra.skeleton.config;

import dev.jsegarra.skeleton.adapter.in.contracts.response.ApiError;
import dev.jsegarra.skeleton.domain.exception.DomainException;
import dev.jsegarra.skeleton.domain.exception.DummyNotFoundException;
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
    private static final String DUMMY_NOT_FOUND_CODE = "DUMMY_NOT_FOUND";

    @ExceptionHandler(DummyNotFoundException.class)
    public ResponseEntity<ApiError> handleDummyNotFound(final DummyNotFoundException exception) {
        final ApiError body = new ApiError(DUMMY_NOT_FOUND_CODE, exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(final DomainException exception) {
        final ApiError body = new ApiError(exception.code(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(final Exception exception) {
        final ApiError body = new ApiError(UNEXPECTED_CODE, UNEXPECTED_MESSAGE);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
