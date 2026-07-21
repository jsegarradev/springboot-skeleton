// Backend hexagon gate — enforcement layer (A), springboot.md §3.1. Runs as a normal JUnit test, so
// `mvn verify` (and therefore CI) fails the build on any violation. Rings are empty on the bare
// skeleton (archunit.properties allows an empty should); the rules activate as slices populate them.
package dev.jsegarra.skeleton;

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

@AnalyzeClasses(packages = "dev.jsegarra.skeleton", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // (A1) the core depends on nothing framework/adapter
    @ArchTest
    static final ArchRule core_is_framework_free = noClasses().that()
            .resideInAnyPackage("..domain..", "..usecase..", "..port..").should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "com.fasterxml.jackson..",
                    "..adapter..");

    // (A2) no framework annotations on core types
    @ArchTest
    static final ArchRule core_has_no_framework_annotations = noClasses().that()
            .resideInAnyPackage("..domain..", "..usecase..", "..port..").should()
            .beAnnotatedWith("org.springframework.stereotype.Service").orShould()
            .beAnnotatedWith("org.springframework.transaction.annotation.Transactional").orShould()
            .beAnnotatedWith("jakarta.persistence.Entity");

    // (A3) dependencies point inward: adapters and use-cases depend on the ports, ports on the domain.
    // Rings (inner→outer): domain < port < usecase < adapters. The composition root (config)
    // wires every ring, so it is exempt. Optional layers so empty rings on the skeleton pass.
    @ArchTest
    static final ArchRule onion = onionArchitecture().domainModels("..domain..").domainServices("..port..")
            .applicationServices("..usecase..").adapter("in", "..adapter.in..").adapter("out", "..adapter.out..")
            .withOptionalLayers(true).ignoreDependency(resideInAPackage("..config.."), alwaysTrue());

    // (A4) inbound adapters program to port/in — never to the use-case impls in ..usecase..
    @ArchTest
    static final ArchRule inbound_adapters_use_ports_not_impls = noClasses().that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAPackage("..usecase..");

    // (A5) inbound adapters stay free of persistence detail (no JPA at the web edge)
    @ArchTest
    static final ArchRule inbound_adapters_free_of_jpa = noClasses().that().resideInAPackage("..adapter.in..").should()
            .dependOnClassesThat().resideInAnyPackage("jakarta.persistence..");

    // (A6) implementations of an outbound port live in adapter/out (the persistence/integration adapter)
    @ArchTest
    static final ArchRule outbound_port_impls_in_adapter_out = classes().that()
            .implement(resideInAPackage("..port.out..").as("an outbound port")).should()
            .resideInAPackage("..adapter.out..");

    // (A7) one operation per use case — each class in ..usecase.. exposes at most one public
    // non-constructor method (getMethods() excludes constructors), enforcing one-op-per-class.
    @ArchTest
    static final ArchRule usecase_exposes_one_operation = classes().that().resideInAPackage("..usecase..")
            .should(new ArchCondition<JavaClass>("expose at most one public non-constructor method") {
                @Override
                public void check(final JavaClass clazz, final ConditionEvents events) {
                    final long publicMethods = clazz.getMethods().stream()
                            .filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC)).count();
                    if (publicMethods > 1) {
                        events.add(SimpleConditionEvent.violated(clazz, clazz.getFullName() + " exposes "
                                + publicMethods + " public methods (expected at most 1)"));
                    }
                }
            });
}
