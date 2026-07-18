package dev.jsegarra.skeleton.config;

import org.springframework.context.annotation.Configuration;

/**
 * Wiring root: one {@code @Bean} per use case, {@code new}-ing the plain use-case implementations and injecting the
 * adapter beans — so use-case impls stay framework-free plain classes (springboot.md §3). Empty until the first slice
 * adds a use case.
 */
@Configuration
public class UseCaseConfig {
}
