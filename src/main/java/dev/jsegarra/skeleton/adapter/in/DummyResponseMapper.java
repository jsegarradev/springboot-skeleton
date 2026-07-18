package dev.jsegarra.skeleton.adapter.in;

import dev.jsegarra.skeleton.adapter.in.contracts.DummyResponseBody;
import dev.jsegarra.skeleton.domain.value.Dummy;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper: domain {@link Dummy} → generated {@link DummyResponseBody} contract (springboot.md §6). Keeps the
 * controller free of hand-written mapping.
 */
@Mapper(componentModel = "spring")
public interface DummyResponseMapper {

    DummyResponseBody toResponse(Dummy dummy);
}
