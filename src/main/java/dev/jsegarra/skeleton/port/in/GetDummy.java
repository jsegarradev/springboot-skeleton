package dev.jsegarra.skeleton.port.in;

import dev.jsegarra.skeleton.domain.value.Dummy;

/**
 * Inbound port: read the dummy value. One operation per port (ISP), one method.
 */
public interface GetDummy {

    Dummy execute();
}
