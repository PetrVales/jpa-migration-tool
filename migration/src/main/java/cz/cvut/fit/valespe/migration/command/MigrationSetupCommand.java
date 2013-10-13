package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.MigrationSetupOperations;
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
    @Reference private MigrationSetupOperations migrationSetupOperations;

    public MigrationSetupCommand() { }

    public MigrationSetupCommand(ProjectOperations projectOperations, MigrationSetupOperations migrationSetupOperations) {
        this.projectOperations = projectOperations;
        this.migrationSetupOperations = migrationSetupOperations;
    }

    @CliAvailabilityIndicator({ "migration setup" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && !migrationSetupOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = "migration setup", help = "Creates migration.xml file")
    public void initMigration() {
        migrationSetupOperations.createMigrationFile();
    }

}
