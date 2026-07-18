package dev.jsegarra.skeleton.adapter.out;

import dev.jsegarra.skeleton.domain.value.Dummy;
import dev.jsegarra.skeleton.port.out.DummyPort;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing the outbound {@link DummyPort}: delegates to the Spring Data repo and maps entity →
 * domain. Transactions live on the Spring side (springboot.md §3, §7).
 */
@Component
public class DummyPersistenceAdapter implements DummyPort {

    private final DummyRepository repository;
    private final DummyEntityMapper mapper;

    public DummyPersistenceAdapter(final DummyRepository repository, final DummyEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dummy> findDummy() {
        return repository.findAll().stream().findFirst().map(mapper::toDomain);
    }
}
