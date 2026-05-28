package project.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.metrics.ArchitectureMetrics;
import com.tngtech.archunit.library.metrics.ComponentDependencyMetrics;
import com.tngtech.archunit.library.metrics.MetricsComponents;

import java.util.Set;

public class MetricsRunner {
    public static void main(String[] args) {
        JavaClasses classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages("project");
        Set<JavaPackage> packages = classes.getPackage("project").getSubpackages();

        MetricsComponents<JavaClass> components = MetricsComponents.fromPackages(packages);

        ComponentDependencyMetrics metrics = ArchitectureMetrics.componentDependencyMetrics(components);

        components.stream().forEach(component -> {
            System.out.println("Component: " + component.getIdentifier());
            System.out.println("Ce: " + metrics.getEfferentCoupling(component.getIdentifier()));
            System.out.println("Ca: " + metrics.getAfferentCoupling(component.getIdentifier()));
            System.out.println("I: " + metrics.getInstability(component.getIdentifier()));
            System.out.println("A: " + metrics.getAbstractness(component.getIdentifier()));
            System.out.println("D: " + metrics.getNormalizedDistanceFromMainSequence(component.getIdentifier()));
        });
    }
}
