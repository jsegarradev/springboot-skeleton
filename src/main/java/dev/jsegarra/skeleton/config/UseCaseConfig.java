package dev.jsegarra.skeleton.config;

import dev.jsegarra.skeleton.port.in.E2e;
import dev.jsegarra.skeleton.port.in.GetDummy;
import dev.jsegarra.skeleton.port.out.DummyPort;
import dev.jsegarra.skeleton.port.out.TransactionRunner;
import dev.jsegarra.skeleton.usecase.E2eUseCase;
import dev.jsegarra.skeleton.usecase.GetDummyUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring root: one {@code @Bean} per use case, {@code new}-ing the plain use-case implementations and injecting the
 * adapter beans — so use-case impls stay framework-free plain classes (springboot.md §3).
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public GetDummy getDummy(final DummyPort dummyPort) {
        return new GetDummyUseCase(dummyPort);
    }

    @Bean
    public E2e e2e(final GetDummy getDummy, final TransactionRunner transactionRunner) {
        return new E2eUseCase(getDummy, transactionRunner);
    }
}
