package dev.jsegarra.skeleton.adapter.in;

import dev.jsegarra.skeleton.adapter.in.contracts.response.E2eResponseBody;
import dev.jsegarra.skeleton.port.in.E2e;
import dev.jsegarra.skeleton.port.in.result.E2eResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Live-verify surface (springboot.md §10.1): one secret-guarded endpoint that drives the standing aggregate e2e journey
 * (every inbound port, rolled back). Safe-by-default — 404 when the secret is unset, and 404 (not 401/403) on a wrong
 * secret so the endpoint stays invisible.
 */
@RestController
public class E2eController {

    private static final String SECRET_HEADER = "x-e2e-secret";

    private final E2e e2e;
    private final String secret;

    public E2eController(final E2e e2e, @Value("${e2e.secret:}") final String secret) {
        this.e2e = e2e;
        this.secret = secret;
    }

    @PostMapping("/internal/e2e")
    public ResponseEntity<E2eResponseBody> run(
            @RequestHeader(value = SECRET_HEADER, required = false) final String providedSecret) {
        if (secret.isBlank() || !secret.equals(providedSecret)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        final E2eResult result = e2e.execute();
        return ResponseEntity.ok(new E2eResponseBody(result.ran(), result.fields()));
    }
}
