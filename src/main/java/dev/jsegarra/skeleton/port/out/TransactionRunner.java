package dev.jsegarra.skeleton.port.out;

import java.util.function.Supplier;

/**
 * Outbound port: run work inside a transaction that always rolls back — the rollback is owned by the adapter (Spring
 * side) while the orchestration stays in the core. The e2e journey uses this so it mutates nothing (springboot.md
 * §10.1).
 */
public interface TransactionRunner {

    <T> T runAndRollback(Supplier<T> action);
}
