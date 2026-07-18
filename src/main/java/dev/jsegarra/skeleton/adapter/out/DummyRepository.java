package dev.jsegarra.skeleton.adapter.out;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link DummyEntity}. Fronted by {@code DummyPersistenceAdapter}, which implements the
 * outbound port — the core never sees this type.
 */
public interface DummyRepository extends JpaRepository<DummyEntity, Long> {
}
