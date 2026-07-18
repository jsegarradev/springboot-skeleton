package dev.jsegarra.skeleton.port.in.result;

import java.util.Map;

/**
 * Result of the e2e journey — the content the post-deploy verifier asserts field-by-field. A core output:
 * annotation-free, mapped to a response DTO by the controller (springboot.md §3, §4).
 *
 * @param ran
 *            whether the journey executed end to end
 * @param fields
 *            the asserted values produced by each driven port
 */
public record E2eResult(boolean ran, Map<String, Object> fields) {
}
