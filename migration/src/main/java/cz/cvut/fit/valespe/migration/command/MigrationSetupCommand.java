package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CommandMarker;

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
    
    @CliCommand(value = "migration setup", help = "Creates migration.xml file")
    public void initMigration() {
        liquibaseOperations.createMigrationFile();
    }

}
