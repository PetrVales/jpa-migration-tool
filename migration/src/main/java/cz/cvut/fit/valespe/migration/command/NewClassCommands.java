package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.NewClassOperations;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.w3c.dom.Element;

import java.sql.Timestamp;
import java.util.Arrays;

@Component
@Service
public class NewClassCommands implements CommandMarker {
    
    @Reference private NewClassOperations newclassOperations;
    @Reference private ProjectOperations projectOperations;
    @Reference private LiquibaseOperations liquibaseOperations;

    public NewClassCommands() { }

    public NewClassCommands(NewClassOperations newclassOperations, ProjectOperations projectOperations, LiquibaseOperations liquibaseOperations) {
        this.newclassOperations = newclassOperations;
        this.projectOperations = projectOperations;
        this.liquibaseOperations = liquibaseOperations;
    }

    @CliAvailabilityIndicator({ "migrate new class" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }

    @CliCommand(value = "migrate new class", help = "Creates a new JPA persistent entity in SRC_MAIN_JAVA and DB migration script in SRC_MAIN_RESOURCES")
    public void newClass(
            @CliOption(key = "class", optionContext = "update,project", mandatory = true, help = "Name of the entity to create") final JavaType className,
            @CliOption(key = "table", mandatory = true, help = "The JPA table name to use for this entity") final String table,
            @CliOption(key = "entityName", mandatory = false, help = "The name used to refer to the entity in queries") final String entityName,
            @CliOption(key = "author", mandatory = false, help = "The name used to refer to the entity in queries") final String author,
            @CliOption(key = "id", mandatory = false, help = "The name used to refer to the entity in queries") final String id) {
        newclassOperations.createEntity(className, entityName == null ? table : entityName, table);
        final Element element = liquibaseOperations.createTable(table);
        liquibaseOperations.createChangeSet(Arrays.asList(element), author, id);
    }

}
