/**
 * Application core — one use-case class per operation, a single {@code execute(...)} method, plain class with
 * constructor injection, implements {@code port/in}, calls {@code port/out}. Keep framework annotations off; wiring
 * lives in {@code config} (springboot.md §3).
 */
@Application
package dev.jsegarra.skeleton.usecase;

import org.jmolecules.architecture.hexagonal.Application;
