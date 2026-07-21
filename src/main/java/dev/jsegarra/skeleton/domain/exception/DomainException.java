package dev.jsegarra.skeleton.domain.exception;

/**
 * Base type for domain-rule violations (springboot.md §3, §5). Unchecked: value objects throw a {@code DomainException}
 * from their compact constructor when an invariant is broken — the core never validates in the use case or controller.
 * {@code GlobalExceptionHandler} maps it to the standard {@code ApiError} body. Subclass per invariant, or throw
 * directly with a stable {@link #code()}.
 */
public class DomainException extends RuntimeException {

    private final String code;

    public DomainException(final String code, final String message) {
        super(message);
        this.code = code;
    }

    /** Stable, machine-readable error code for the {@code ApiError} body (not the HTTP status). */
    public String code() {
        return code;
    }
}
