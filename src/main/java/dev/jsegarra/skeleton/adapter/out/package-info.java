/**
 * Outbound adapters (secondary) — JPA entities, Spring Data repositories, and the persistence adapter classes that
 * implement {@code port/out}, mapping entity↔domain. {@code @Transactional} lives here (springboot.md §3, §7).
 */
@SecondaryAdapter
package dev.jsegarra.skeleton.adapter.out;

import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
