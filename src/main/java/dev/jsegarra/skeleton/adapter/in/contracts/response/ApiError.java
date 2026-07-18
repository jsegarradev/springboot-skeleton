package dev.jsegarra.skeleton.adapter.in.contracts.response;

/**
 * Standard error-response shape returned by {@code GlobalExceptionHandler} (springboot.md §3).
 *
 * @param code
 *            a stable, machine-readable error code (not the HTTP status)
 * @param message
 *            a human-readable description safe to expose to the client
 */
public record ApiError(String code, String message) {
}
