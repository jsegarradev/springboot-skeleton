package dev.jsegarra.skeleton.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.jsegarra.skeleton.domain.value.Dummy;
import dev.jsegarra.skeleton.port.in.GetDummy;
import dev.jsegarra.skeleton.port.in.result.E2eResult;
import dev.jsegarra.skeleton.port.out.TransactionRunner;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class E2eUseCaseTest {

    private static final String DUMMY_VALUE = "walking-skeleton";

    @Mock
    private GetDummy getDummy;

    @Mock
    private TransactionRunner transactionRunner;

    @Test
    void drives_every_inbound_port_inside_the_rollback_transaction() {
        when(transactionRunner.runAndRollback(any()))
                .thenAnswer(invocation -> invocation.getArgument(0, Supplier.class).get());
        when(getDummy.execute()).thenReturn(new Dummy(1L, DUMMY_VALUE));

        final E2eUseCase useCase = new E2eUseCase(getDummy, transactionRunner);

        final E2eResult result = useCase.execute();

        assertThat(result.ran()).isTrue();
        assertThat(result.fields()).containsEntry("dummy", DUMMY_VALUE);
        verify(getDummy).execute();
    }
}
