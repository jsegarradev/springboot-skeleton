// Backend hexagon gate (always). Runs as a normal JUnit test, so `mvn test`/`verify` — and therefore
// CI — fails the build on any violation. Set the base package below to com.<org>.<app>.
// NOTE: confirm the exact ArchUnit DSL on the first compile (APIs shift between versions); adjust the
// package globs to your layout (e.g. per-Modulith-module packages in a monolith).
package com.example;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "com.example", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // (A1) the core depends on nothing framework/adapter
    @ArchTest
    static final ArchRule core_is_framework_free = noClasses()
        .that().resideInAnyPackage("..domain..", "..usecase..", "..port..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("org.springframework..", "jakarta.persistence..",
            "com.fasterxml.jackson..", "..adapter..");

    // (A2) no framework annotations on core types
    @ArchTest
    static final ArchRule core_has_no_framework_annotations = noClasses()
        .that().resideInAnyPackage("..domain..", "..usecase..")
        .should().beAnnotatedWith("org.springframework.stereotype.Service")
        .orShould().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
        .orShould().beAnnotatedWith("jakarta.persistence.Entity");

    // (A3) dependencies point inward: adapters and use-cases depend on the ports, ports on the domain.
    //      Rings (inner→outer): domain < port < usecase < adapters. The composition root (config)
    //      wires every ring, so it is exempt. (Validated on a real slice; JMolecules' ensureHexagonal
    //      is the semantic cross-check.)
    @ArchTest
    static final ArchRule onion = onionArchitecture()
        .domainModels("..domain..")
        .domainServices("..port..")
        .applicationServices("..usecase..")
        .adapter("in", "..adapter.in..")
        .adapter("out", "..adapter.out..")
        .ignoreDependency(resideInAPackage("..config.."), alwaysTrue());

    // (A4) inbound adapters program to port/in — never to the use-case impls in ..usecase..
    @ArchTest
    static final ArchRule inbound_adapters_use_ports_not_impls = noClasses()
        .that().resideInAPackage("..adapter.in..")
        .should().dependOnClassesThat().resideInAPackage("..usecase..");

    // (A5) inbound adapters stay free of persistence detail (no JPA at the web edge)
    @ArchTest
    static final ArchRule inbound_adapters_free_of_jpa = noClasses()
        .that().resideInAPackage("..adapter.in..")
        .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..");

    // (A6) implementations of an outbound port live in adapter/out (the persistence/integration adapter)
    @ArchTest
    static final ArchRule outbound_port_impls_in_adapter_out = classes()
        .that().implement(resideInAPackage("..port.out..").as("an outbound port"))
        .should().resideInAPackage("..adapter.out..");

    // (A7) one operation per use case — each class in ..usecase.. exposes at most one public
    //      non-constructor method (getMethods() excludes constructors), enforcing one-operation-per-class.
    @ArchTest
    static final ArchRule usecase_exposes_one_operation = classes()
        .that().resideInAPackage("..usecase..")
        .should(new ArchCondition<JavaClass>("expose at most one public non-constructor method") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                long publicMethods = clazz.getMethods().stream()
                    .filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC))
                    .count();
                if (publicMethods > 1) {
                    events.add(SimpleConditionEvent.violated(clazz, clazz.getFullName()
                        + " exposes " + publicMethods + " public methods (expected at most 1)"));
                }
            }
        });

    // NOTE: JMolecules is the semantic alternative — annotate types @Domain/@Application/@Port/@Adapter
    // and replace the hexagon rules (A1)-(A6) with the ready-made `jmolecules-archunit` rules. (A7) is an
    // extra ISP guard beyond JMolecules — keep it either way.
}
