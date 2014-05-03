package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.FieldOperations;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import cz.cvut.fit.valespe.migration.util.FieldCommons;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

import java.util.Arrays;

@Component
@Service
public class RemovePropertyCommands implements CommandMarker {

    private static final String REMOVE_PROPERTY = "migrate remove property";
    private static final String REMOVE_PROPERTY_HELP = "Remove field from class. Generate related liquibase change set.";

    @Reference private FieldOperations fieldOperations;
    @Reference private ProjectOperations projectOperations;
    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private FieldCommons fieldCommons;
    @Reference private ClassCommons classCommons;

    public RemovePropertyCommands() { }

    public RemovePropertyCommands(
            FieldOperations fieldOperations,
            ProjectOperations projectOperations,
            LiquibaseOperations liquibaseOperations,
            FieldCommons fieldCommons,
            ClassCommons classCommons
    ) {
        this.fieldOperations = fieldOperations;
        this.projectOperations = projectOperations;
        this.liquibaseOperations = liquibaseOperations;
        this.fieldCommons = fieldCommons;
        this.classCommons = classCommons;
    }

    @CliAvailabilityIndicator({ REMOVE_PROPERTY })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = REMOVE_PROPERTY, help = REMOVE_PROPERTY_HELP)
    public void removeProperty(
            @CliOption(key = "class", mandatory = true, help = "Name of type where field should be removed") JavaType typeName,
            @CliOption(key = {"", "property"}, mandatory = true, help = "Name of field to remove") JavaSymbolName propertyName,
            @CliOption(key = "skipDrop", mandatory = false, help = "skip dropping any data", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean skipDrop,
            @CliOption(key = "author", mandatory = false, help = "Change set author") final String author,
            @CliOption(key = "id", mandatory = false, help = "Change set id") final String id
    ) {
        Validate.isTrue(classCommons.exist(typeName), "The specified class, '%s', doesn't exist", typeName);
        Validate.isTrue(classCommons.hasField(typeName, propertyName), "The specified property, '%s', of class, %s, doesn't exist", propertyName, typeName);

        String tableName = classCommons.tableName(typeName);
        FieldMetadata field = classCommons.field(typeName, propertyName);
        String columnName = fieldCommons.columnName(field);
        fieldOperations.removeField(propertyName, typeName);
        if (!skipDrop)
            liquibaseOperations.createChangeSet(
                    Arrays.asList(
                            liquibaseOperations.dropColumn(tableName, columnName)
                    ), author, id);
    }

}
