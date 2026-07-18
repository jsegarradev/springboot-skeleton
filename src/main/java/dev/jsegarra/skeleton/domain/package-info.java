/**
 * Domain model — plain Java, framework-free (springboot.md §3, §5).
 *
 * <p>
 * Types carry jMolecules DDD building-block stereotypes from {@code org.jmolecules.ddd.annotation}
 * ({@code @AggregateRoot} / {@code @Entity} / {@code @ValueObject} / {@code @Repository} / {@code @Service}) — there is
 * intentionally <em>no</em> package-level {@code @Domain} annotation. Sub-split by kind: {@code entity}, {@code value},
 * {@code enums}, {@code service}, {@code exception}.
 */
package dev.jsegarra.skeleton.domain;
