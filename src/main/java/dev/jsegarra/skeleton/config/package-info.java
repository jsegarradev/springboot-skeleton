/**
 * Composition root (Spring side) — bean wiring ({@link dev.jsegarra.skeleton.config.UseCaseConfig}), the
 * {@code @RestControllerAdvice} error handler, and app config. Exempt from the inward-dependency rule because it wires
 * every ring (springboot.md §3).
 */
package dev.jsegarra.skeleton.config;
