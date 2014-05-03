package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.FieldOperations;
import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import cz.cvut.fit.valespe.migration.util.FieldCommons;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

import java.util.Arrays;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

@Component
@Service
public class MakePkCommands implements CommandMarker {

    @Reference private FieldOperations fieldOperations;
    @Reference private ProjectOperations projectOperations;
    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private ClassCommons classCommons;
    @Reference private FieldCommons fieldCommons;

    @CliAvailabilityIndicator({ "migrate make pk" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }

    @CliCommand(value = "migrate make pk", help = "Some helpful description")
    public void makePk(
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = {"", "property"}, mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName propertyName,
            @CliOption(key = "skipDrop", mandatory = false, help = "skip dropping any data", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean skipDrop,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        Validate.isTrue(classCommons.exist(typeName), "Specified class, '%s', doesn't exist", typeName);
        if (classCommons.hasField(typeName, propertyName))
            Validate.isTrue(classCommons.hasField(typeName, propertyName), "Specified class, '%s' doesn't have property %s already", typeName, propertyName);

        final String tableName = classCommons.tableName(typeName);
        final String columnName = fieldCommons.columnName(classCommons.field(typeName, propertyName));

        fieldOperations.makeFieldId(typeName, propertyName);
        liquibaseOperations.createChangeSet(Arrays.asList(
                liquibaseOperations.addPrimaryKey(Arrays.asList(columnName), tableName, columnName + "_pk")
        ), author, id);
    }

}
