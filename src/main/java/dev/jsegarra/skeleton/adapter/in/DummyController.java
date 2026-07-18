package dev.jsegarra.skeleton.adapter.in;

import dev.jsegarra.skeleton.adapter.in.contracts.DummyApi;
import dev.jsegarra.skeleton.adapter.in.contracts.DummyResponseBody;
import dev.jsegarra.skeleton.port.in.GetDummy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin inbound adapter: implements the generated {@link DummyApi} contract, calls the inbound port, and maps the core
 * output to the response DTO — no core type is serialized raw (springboot.md §3).
 */
@RestController
public class DummyController implements DummyApi {

    private final GetDummy getDummy;
    private final DummyResponseMapper mapper;

    public DummyController(final GetDummy getDummy, final DummyResponseMapper mapper) {
        this.getDummy = getDummy;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<DummyResponseBody> getDummy() {
        return ResponseEntity.ok(mapper.toResponse(getDummy.execute()));
    }
}
