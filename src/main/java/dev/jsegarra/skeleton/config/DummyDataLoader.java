package dev.jsegarra.skeleton.config;

import dev.jsegarra.skeleton.adapter.out.DummyEntity;
import dev.jsegarra.skeleton.adapter.out.DummyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the single dummy row into the Liquibase-created table, idempotently — seed data is loaded in Java, not in a
 * changeset (springboot.md §7).
 */
@Component
public class DummyDataLoader implements CommandLineRunner {

    private static final String SEED_VALUE = "walking-skeleton";

    private final DummyRepository repository;

    public DummyDataLoader(final DummyRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(final String... args) {
        if (repository.count() == 0) {
            final DummyEntity entity = new DummyEntity();
            entity.setValue(SEED_VALUE);
            repository.save(entity);
        }
    }
}
