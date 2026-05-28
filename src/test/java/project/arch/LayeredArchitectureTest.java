package project.arch;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = ".")
public class LayeredArchitectureTest {
    @ArchTest
    public static final ArchRule layer_dependencies_are_respected = layeredArchitecture()

            .layer("Boundaries").definedBy("gui..")
            .layer("Controls").definedBy("controller..")
            .layer("Entities").definedBy("model..")
            .layer("Database").definedBy("implementazioneDao..")

            .whereLayer("Boundaries").mayNotBeAccessedByAnyLayer()
            .whereLayer("Controls").mayOnlyBeAccessedByLayers("Boundaries")
            .whereLayer("Entities").mayOnlyBeAccessedByLayers("Controls")
            .whereLayer("Database").mayOnlyBeAccessedByLayers("Controls");
}
