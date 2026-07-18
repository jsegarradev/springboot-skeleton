package dev.jsegarra.skeleton.domain.value;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * The dummy value read from the seeded row — a throwaway walking-skeleton read model. Modelled as a value object (no
 * identity lifecycle; replaced wholesale) per springboot.md §5.
 */
@ValueObject
public record Dummy(long id, String value) {
}
