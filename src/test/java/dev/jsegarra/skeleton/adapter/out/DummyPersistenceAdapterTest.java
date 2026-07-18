package dev.jsegarra.skeleton.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jsegarra.skeleton.domain.value.Dummy;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Persistence slice against the H2 stand-in (Liquibase creates the table): the adapter maps the entity to the domain
 * value object. The adapter + generated mapper are imported into the slice.
 */
@DataJpaTest
@Import({DummyPersistenceAdapter.class, DummyEntityMapperImpl.class})
class DummyPersistenceAdapterTest {

    @Autowired
    private DummyRepository repository;

    @Autowired
    private DummyPersistenceAdapter adapter;

    @Test
    void find_dummy_maps_entity_to_domain() {
        final DummyEntity entity = new DummyEntity();
        entity.setValue("mapped-value");
        repository.save(entity);

        final Optional<Dummy> found = adapter.findDummy();

        assertThat(found).map(Dummy::value).contains("mapped-value");
    }
}
