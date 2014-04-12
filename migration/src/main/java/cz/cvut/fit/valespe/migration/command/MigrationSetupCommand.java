package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.DependencyScope;
import org.springframework.roo.project.DependencyType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CommandMarker;

import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class MigrationSetupCommand implements CommandMarker {

    @Reference private ProjectOperations projectOperations;
    @Reference private LiquibaseOperations liquibaseOperations;

    public MigrationSetupCommand() { }

    public MigrationSetupCommand(ProjectOperations projectOperations, LiquibaseOperations liquibaseOperations) {
        this.projectOperations = projectOperations;
        this.liquibaseOperations = liquibaseOperations;
    }

    @CliAvailabilityIndicator({ "migration setup" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && !liquibaseOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = "migration setup", help = "Initialize migration tool. Creates migration.xml file and add maven dependencies.")
    public void initMigration() {
        List<Dependency> dependencies = new ArrayList<Dependency>();
        dependencies.add(
                new Dependency(
                        "cz.cvut.fit.valespe.migration",
                        "cz.cvut.fit.valespe.migration",
                        "0.1.0.BUILD-SNAPSHOT",
                        DependencyType.JAR,
                        DependencyScope.COMPILE
                ));
        projectOperations.addDependencies("", dependencies);
        liquibaseOperations.createMigrationFile();
    }

}
