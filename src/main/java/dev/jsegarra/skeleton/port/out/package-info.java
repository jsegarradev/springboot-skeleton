/**
 * Outbound ports (secondary) — interfaces the core needs from the outside, one method each. Query/read results live in
 * {@code port/out/result} (springboot.md §3, §4).
 */
@SecondaryPort
package dev.jsegarra.skeleton.port.out;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
