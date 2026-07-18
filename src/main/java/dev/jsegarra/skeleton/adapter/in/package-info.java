/**
 * Inbound adapters (primary) — thin REST controllers plus the generated/hand-written contracts under
 * {@code adapter/in/contracts}. Controllers depend on {@code port/in}, never on the use-case impls, and map core
 * outputs to response DTOs — no core type is serialized raw (springboot.md §3).
 */
@PrimaryAdapter
package dev.jsegarra.skeleton.adapter.in;

import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
