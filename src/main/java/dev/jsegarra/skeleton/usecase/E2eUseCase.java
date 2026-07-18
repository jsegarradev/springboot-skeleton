package dev.jsegarra.skeleton.usecase;

import dev.jsegarra.skeleton.domain.value.Dummy;
import dev.jsegarra.skeleton.port.in.E2e;
import dev.jsegarra.skeleton.port.in.GetDummy;
import dev.jsegarra.skeleton.port.in.result.E2eResult;
import dev.jsegarra.skeleton.port.out.TransactionRunner;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standing aggregate e2e journey (springboot.md §10.1): drives every inbound port against the real dependencies inside
 * a rolled-back transaction, returning a content-asserting result. <b>Rule:</b> every new {@code port/in} is wired in
 * here and asserted before its slice is done, so live coverage tracks the entry-point set by construction.
 */
public class E2eUseCase implements E2e {

    private static final String DUMMY_FIELD = "dummy";

    private final GetDummy getDummy;
    private final TransactionRunner transactionRunner;

    public E2eUseCase(final GetDummy getDummy, final TransactionRunner transactionRunner) {
        this.getDummy = getDummy;
        this.transactionRunner = transactionRunner;
    }

    @Override
    public E2eResult execute() {
        return transactionRunner.runAndRollback(() -> {
            final Map<String, Object> fields = new LinkedHashMap<>();
            final Dummy dummy = getDummy.execute();
            fields.put(DUMMY_FIELD, dummy.value());
            return new E2eResult(true, fields);
        });
    }
}
