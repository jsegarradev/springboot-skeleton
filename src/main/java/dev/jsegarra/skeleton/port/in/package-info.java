/**
 * Inbound ports (primary) — use-case interfaces, one per operation (ISP: one method each). Inputs live in
 * {@code port/in/command}, outputs in {@code port/in/result} (springboot.md §3, §4).
 */
@PrimaryPort
package dev.jsegarra.skeleton.port.in;

import org.jmolecules.architecture.hexagonal.PrimaryPort;
