package com.pvg.governance.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Fitness functions enforcing the Controller → Service → Repository layering from
 * day one (proposal §4 — "eating our own dog food", per Module 5 of the learning
 * path). These run in the normal test suite and fail the build on violation.
 */
@AnalyzeClasses(packages = "com.pvg.governance", importOptions = ImportOption.DoNotIncludeTests.class)
class LayeringArchitectureTest {

    @ArchTest
    static final ArchRule layering = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Web").definedBy("com.pvg.governance.web..")
            .layer("Service").definedBy("com.pvg.governance.service..")
            .layer("Repository").definedBy("com.pvg.governance.repository..")
            .layer("Api").definedBy("com.pvg.governance.api..")
            .whereLayer("Web").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Web")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
            .whereLayer("Api").mayOnlyBeAccessedByLayers("Web", "Service");

    @ArchTest
    static final ArchRule controllersDoNotTouchRepositories = noClasses()
            .that().resideInAPackage("com.pvg.governance.web..")
            .should().dependOnClassesThat().resideInAPackage("com.pvg.governance.repository..");

    @ArchTest
    static final ArchRule domainStaysFrameworkAgnosticOfWeb = noClasses()
            .that().resideInAPackage("com.pvg.governance.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.pvg.governance.web..", "com.pvg.governance.api..",
                    "com.pvg.governance.service..", "com.pvg.governance.repository..");

    @ArchTest
    static final ArchRule controllersAreNamedConsistently = classes()
            .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().haveSimpleNameEndingWith("Controller")
            .andShould().resideInAPackage("com.pvg.governance.web..");
}
