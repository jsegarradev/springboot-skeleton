package dev.jsegarra.skeleton.adapter.in.contracts.response;

import java.util.Map;

/**
 * Response DTO for the e2e journey (hand-written; the internal endpoint is not in the public OpenAPI spec). The
 * post-deploy verifier asserts these fields (springboot.md §10.1).
 *
 * @param ran
 *            whether the journey executed end to end
 * @param fields
 *            the asserted values produced by each driven port
 */
public record E2eResponseBody(boolean ran, Map<String, Object> fields) {
}
