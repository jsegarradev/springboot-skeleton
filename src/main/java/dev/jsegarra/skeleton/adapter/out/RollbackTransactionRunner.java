package dev.jsegarra.skeleton.adapter.out;

import dev.jsegarra.skeleton.port.out.TransactionRunner;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs work inside a transaction forced to roll back, so the e2e journey exercises the real path yet mutates nothing
 * (springboot.md §10.1).
 */
@Component
public class RollbackTransactionRunner implements TransactionRunner {

    private final TransactionTemplate transactionTemplate;

    public RollbackTransactionRunner(final PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T runAndRollback(final Supplier<T> action) {
        return transactionTemplate.execute(status -> {
            try {
                return action.get();
            } finally {
                status.setRollbackOnly();
            }
        });
    }
}
