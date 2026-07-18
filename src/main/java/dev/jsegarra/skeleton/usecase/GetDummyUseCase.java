package dev.jsegarra.skeleton.usecase;

import dev.jsegarra.skeleton.domain.exception.DummyNotFoundException;
import dev.jsegarra.skeleton.domain.value.Dummy;
import dev.jsegarra.skeleton.port.in.GetDummy;
import dev.jsegarra.skeleton.port.out.DummyPort;

/**
 * Reads the dummy value through the outbound port; throws {@link DummyNotFoundException} (→ 404) when the seeded row is
 * missing. Plain class, constructor injection — wired in {@code UseCaseConfig}.
 */
public class GetDummyUseCase implements GetDummy {

    private final DummyPort dummyPort;

    public GetDummyUseCase(final DummyPort dummyPort) {
        this.dummyPort = dummyPort;
    }

    @Override
    public Dummy execute() {
        return dummyPort.findDummy().orElseThrow(DummyNotFoundException::new);
    }
}
