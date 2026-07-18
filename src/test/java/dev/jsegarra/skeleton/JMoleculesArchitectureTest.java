// Enforcement layer (C), springboot.md §3.1: the jMolecules ready-made ArchUnit rules verify the
// hexagonal stereotypes (@PrimaryPort / @SecondaryPort / @PrimaryAdapter / @SecondaryAdapter /
// @Application, declared on each ring's package-info) and the DDD building-block constraints. Runs
// alongside the hand-rolled A1–A7 rules in ArchitectureTest.
package dev.jsegarra.skeleton;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;

@AnalyzeClasses(packages = "dev.jsegarra.skeleton", importOptions = ImportOption.DoNotIncludeTests.class)
class JMoleculesArchitectureTest {

    // Hexagonal stereotype dependencies point the right way (adapters → ports/application → domain).
    @ArchTest
    static final ArchRule hexagonal = JMoleculesArchitectureRules.ensureHexagonal();

    // DDD building blocks respect their constraints (e.g. cross-aggregate refs via id/association).
    @ArchTest
    static final ArchRule ddd = JMoleculesDddRules.all();
}
