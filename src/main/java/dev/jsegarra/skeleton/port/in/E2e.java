package dev.jsegarra.skeleton.port.in;

import dev.jsegarra.skeleton.port.in.result.E2eResult;

/**
 * Inbound port for the standing aggregate e2e journey (springboot.md §10.1): drives every inbound port against real
 * dependencies, rolled back, returning a content-asserting result.
 */
public interface E2e {

    E2eResult execute();
}
