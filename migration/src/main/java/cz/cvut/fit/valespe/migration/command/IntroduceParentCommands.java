package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.ClassOperations;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class IntroduceParentCommands implements CommandMarker {

    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private ProjectOperations projectOperations;
    @Reference private ClassOperations classOperations;

    @CliAvailabilityIndicator({ "migrate introduce parent" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }

    @CliCommand(value = "migrate introduce parent", help = "Merge two classes into one and generate migration")
    public void introduceParent(
            @CliOption(key = "class", mandatory = true, help = "The java type to apply this annotation to") JavaType target,
            @CliOption(key = "parent", mandatory = true, help = "The java type to apply this annotation to") JavaType parent,
            @CliOption(key = "parentTable", mandatory = false, help = "The java type to apply this annotation to") String parentTable,
            @CliOption(key = "parentEntity", mandatory = false, help = "The java type to apply this annotation to") String parentEntity,
            @CliOption(key = "author", mandatory = false, help = "The name used to refer to the entity in queries") final String author,
            @CliOption(key = "id", mandatory = false, help = "The name used to refer to the entity in queries") final String id) {
        if (typeLocationService.getTypeDetails(parent) == null) {
            Validate.notBlank(parentTable, "--parentTable is not specified.");
            classOperations.createClass(parent, parentEntity == null ? parentTable : parentEntity, parentTable);
        }
        classOperations.introduceParent(target, parent);
    }

}
