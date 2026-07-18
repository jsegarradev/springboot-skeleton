package dev.jsegarra.skeleton.port.out;

import dev.jsegarra.skeleton.domain.value.Dummy;
import java.util.Optional;

/**
 * Outbound port: load the dummy value. One operation per port (ISP), one method.
 */
public interface DummyPort {

    Optional<Dummy> findDummy();
}
