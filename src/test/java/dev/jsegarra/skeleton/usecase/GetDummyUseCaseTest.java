package dev.jsegarra.skeleton.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import dev.jsegarra.skeleton.domain.exception.DummyNotFoundException;
import dev.jsegarra.skeleton.domain.value.Dummy;
import dev.jsegarra.skeleton.port.out.DummyPort;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetDummyUseCaseTest {

    @Mock
    private DummyPort dummyPort;

    @Test
    void returns_the_dummy_from_the_port() {
        final Dummy dummy = new Dummy(1L, "walking-skeleton");
        when(dummyPort.findDummy()).thenReturn(Optional.of(dummy));

        final GetDummyUseCase useCase = new GetDummyUseCase(dummyPort);

        assertThat(useCase.execute()).isEqualTo(dummy);
    }

    @Test
    void throws_when_no_dummy_row_exists() {
        when(dummyPort.findDummy()).thenReturn(Optional.empty());

        final GetDummyUseCase useCase = new GetDummyUseCase(dummyPort);

        assertThatThrownBy(useCase::execute).isInstanceOf(DummyNotFoundException.class);
    }
}
