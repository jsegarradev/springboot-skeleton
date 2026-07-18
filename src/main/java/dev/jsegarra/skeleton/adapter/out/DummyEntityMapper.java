package dev.jsegarra.skeleton.adapter.out;

import dev.jsegarra.skeleton.domain.value.Dummy;
import org.mapstruct.Mapper;

/**
 * MapStruct entity → domain mapper. The {@code lombok-mapstruct-binding} processor lets it read Lombok's generated
 * getters (springboot.md §7).
 */
@Mapper(componentModel = "spring")
public interface DummyEntityMapper {

    Dummy toDomain(DummyEntity entity);
}
