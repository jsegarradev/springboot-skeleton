package dev.jsegarra.skeleton.domain.exception;

/**
 * Raised when no dummy row exists. Mapped to HTTP 404 by the {@code GlobalExceptionHandler}.
 */
public class DummyNotFoundException extends RuntimeException {

    private static final String MESSAGE = "No dummy row found";

    public DummyNotFoundException() {
        super(MESSAGE);
    }
}
